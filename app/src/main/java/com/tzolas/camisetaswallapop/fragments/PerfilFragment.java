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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.LoginActivity;
import com.tzolas.camisetaswallapop.adapters.CamisetaAdapter;
import com.tzolas.camisetaswallapop.models.Camiseta;

import java.util.ArrayList;
import java.util.List;

public class PerfilFragment extends Fragment {

    private TextView tvName, tvEmail, tvEmpty;
    private ImageView ivProfilePhoto;
    private Button btnLogout;
    private RecyclerView recyclerView;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<Camiseta> camisetaList;
    private CamisetaAdapter camisetaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // 🔹 Referencias UI
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        btnLogout = view.findViewById(R.id.btnLogout);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // 🔹 Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        camisetaList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        camisetaAdapter = new CamisetaAdapter(getContext(), camisetaList);
        recyclerView.setAdapter(camisetaAdapter);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            mostrarDatosUsuario(user);
            cargarCamisetasUsuario(user.getUid());
        } else {
            Toast.makeText(getContext(), "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
        }

        // 🔹 Botón cerrar sesión
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

        // Foto de perfil
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

    private void cargarCamisetasUsuario(String userId) {
        db.collection("camisetas")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    camisetaList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Camiseta camiseta = doc.toObject(Camiseta.class);
                        camisetaList.add(camiseta);
                    }

                    if (camisetaList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    camisetaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar el armario", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        // 🔁 Recargar camisetas por si el usuario subió una nueva
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            cargarCamisetasUsuario(user.getUid());
        }
    }
}
