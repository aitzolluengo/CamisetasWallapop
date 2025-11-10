package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.LoginActivity;

public class PerfilFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfilePhoto;
    private Button btnLogout;

    private FirebaseAuth auth;

    public PerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        btnLogout = view.findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            mostrarDatosUsuario(user);
        } else {
            Toast.makeText(getContext(), "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void mostrarDatosUsuario(FirebaseUser user) {

        // Nombre
        String name = user.getDisplayName();
        tvName.setText((name != null && !name.isEmpty()) ? name : "Usuario sin nombre");

        // Email
        tvEmail.setText(user.getEmail());

        // Foto perfil
        Uri photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(ivProfilePhoto);
        } else {
            ivProfilePhoto.setImageResource(R.drawable.ic_user_placeholder);
        }
    }
}
