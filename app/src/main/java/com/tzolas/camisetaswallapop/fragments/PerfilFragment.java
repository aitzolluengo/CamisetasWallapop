package com.tzolas.camisetaswallapop.fragments;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.LoginActivity;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class PerfilFragment extends Fragment {

    private TextView tvName, tvEmail, tvEmpty;
    private ImageView ivProfilePhoto;
    private Button btnLogout;
    private FirebaseAuth auth;

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private List<Product> listaProductos = new ArrayList<>();

    public PerfilFragment() {}

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // UI
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductsAdapter(listaProductos);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            mostrarDatosUsuario(user);
            cargarMisProductos(user.getUid());
        } else {
            Toast.makeText(getContext(), "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            requireActivity().finish();
        });

        return view;
    }

    private void mostrarDatosUsuario(FirebaseUser user) {
        String name = user.getDisplayName();
        tvName.setText((name != null && !name.isEmpty()) ? name : "Usuario sin nombre");
        tvEmail.setText(user.getEmail());

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

    private void cargarMisProductos(String uid) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    listaProductos.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) listaProductos.add(p);
                    }

                    adapter.notifyDataSetChanged();

                    // Mostrar/ocultar mensaje vacío
                    if (listaProductos.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error cargando productos", Toast.LENGTH_SHORT).show()
                );
    }
}
