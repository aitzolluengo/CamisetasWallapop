package com.tzolas.camisetaswallapop.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ProductDetailActivity;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PerfilFragment extends Fragment {

    private static final int PICK_IMAGE = 2001;

    private TextView tvName, tvEmail, tvEmpty, txtRatingCount;
    private ImageView ivProfilePhoto;
    private RatingBar ratingBar;

    private Button btnLogout, btnEditProfile;
    private Button btnEnVenta, btnComprados;

    private RecyclerView recyclerVenta, recyclerComprados;
    private ProductsAdapter ventaAdapter, compradosAdapter;

    private final List<Product> listaVenta = new ArrayList<>();
    private final List<Product> listaComprados = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private ListenerRegistration ratingListener;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // UI inicial
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        txtRatingCount = view.findViewById(R.id.txtRatingCount);
        ratingBar = view.findViewById(R.id.ratingBarProfile);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        btnEnVenta = view.findViewById(R.id.btnEnVenta);
        btnComprados = view.findViewById(R.id.btnComprados);

        recyclerVenta = view.findViewById(R.id.recyclerVenta);
        recyclerComprados = view.findViewById(R.id.recyclerComprados);

        // Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Mostrar datos usuario
        if (user != null) {
            mostrarDatosUsuario(user);
            cargarProductosVentaYComprados(user.getUid());
        }

        // EDITAR PERFIL (solo foto por ahora)
        btnEditProfile.setOnClickListener(v -> {
            // mostrar menú con opciones
            mostrarOpcionesEditarPerfil();
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {
            if (ratingListener != null) ratingListener.remove(); // por si acaso
            auth.signOut();

            // cerrar actividad completa
            requireActivity().finishAffinity();
        });


        // CONFIG LISTAS
        ventaAdapter = new ProductsAdapter(requireContext(), listaVenta,
                p -> abrirDetalle(p.getId()));

        compradosAdapter = new ProductsAdapter(requireContext(), listaComprados,
                p -> abrirDetalle(p.getId()));

        recyclerVenta.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComprados.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerVenta.setAdapter(ventaAdapter);
        recyclerComprados.setAdapter(compradosAdapter);

        btnEnVenta.setOnClickListener(v -> mostrarVenta());
        btnComprados.setOnClickListener(v -> mostrarComprados());

        return view;
    }

    private void mostrarDatosUsuario(FirebaseUser user) {
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Sin nombre");
        tvEmail.setText(user.getEmail());

        Uri photo = user.getPhotoUrl();
        Glide.with(this)
                .load(photo != null ? photo : R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(ivProfilePhoto);
    }

    /** 🔥 Cargar lista de productos en venta/vendidos y comprados */
    private void cargarProductosVentaYComprados(String uid) {
        // Productos que yo subí
        db.collection("products")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(q -> {
                    listaVenta.clear();
                    for (DocumentSnapshot doc : q) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaVenta.add(p);
                        }
                    }
                    ventaAdapter.notifyDataSetChanged();
                });

        // Productos donde yo soy buyerId (he comprado)
        db.collection("products")
                .whereEqualTo("buyerId", uid)
                .get()
                .addOnSuccessListener(q -> {
                    listaComprados.clear();
                    for (DocumentSnapshot doc : q) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaComprados.add(p);
                        }
                    }
                    compradosAdapter.notifyDataSetChanged();
                });
    }

    private void abrirDetalle(String id) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("productId", id);
        startActivity(intent);
    }

    /** Cambiar entre listas */
    private void mostrarVenta() {
        recyclerVenta.setVisibility(View.VISIBLE);
        recyclerComprados.setVisibility(View.GONE);
    }

    private void mostrarComprados() {
        recyclerVenta.setVisibility(View.GONE);
        recyclerComprados.setVisibility(View.VISIBLE);
    }

    /** Editar foto */
    private void abrirGaleria() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int r, int res, @Nullable Intent data) {
        super.onActivityResult(r, res, data);
        if (r == PICK_IMAGE && res == Activity.RESULT_OK && data != null) {
            Uri img = data.getData();
            ivProfilePhoto.setImageURI(img);
            // Aquí puedes actualizar Storage + Firestore si quieres
        }
    }
    private void mostrarDialogoEditarNombre() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_name, null, false);

        EditText edtName = view.findViewById(R.id.edtNewName);

        edtName.setText(tvName.getText()); // mostrar nombre actual

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Editar nombre")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = edtName.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    actualizarNombreEnFirebase(nuevoNombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void actualizarNombreEnFirebase(String nuevoNombre) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // 1) Actualizar en Firebase Auth
        user.updateProfile(
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nuevoNombre)
                        .build()
        ).addOnSuccessListener(a -> {

            tvName.setText(nuevoNombre); // actualizar en pantalla

            // 2) Actualizar en Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("name", nuevoNombre)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(getContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
    private void mostrarOpcionesEditarPerfil() {

        String[] opciones = {"Cambiar foto", "Cambiar nombre"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirGaleria();
                    } else if (which == 1) {
                        mostrarDialogoEditarNombre();
                    }
                })
                .show();
    }



}
