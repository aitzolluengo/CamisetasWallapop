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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ProductDetailActivity;
import com.tzolas.camisetaswallapop.adapters.OffersAdapter;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.utils.CloudinaryUploader;
import com.tzolas.camisetaswallapop.activities.BlockedUsersActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private static final int PICK_IMAGE = 2001;

    private TextView tvName, tvEmail, txtRatingCount, txtOffersTitle;
    private ImageView ivProfilePhoto;
    private RatingBar ratingBar;

    private Button btnLogout, btnEditProfile;
    private Button btnEnVenta, btnComprados, btnBlockedUsers;

    private RecyclerView recyclerVenta, recyclerComprados, recyclerOffers;

    private ProductsAdapter ventaAdapter, compradosAdapter;
    private OffersAdapter offersAdapter;

    private final List<Product> listaVenta = new ArrayList<>();
    private final List<Product> listaComprados = new ArrayList<>();
    private final List<Map<String, Object>> offersList = new ArrayList<>();

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

        // UI
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        txtRatingCount = view.findViewById(R.id.txtRatingCount);
        txtOffersTitle = view.findViewById(R.id.txtOffersTitle);
        ratingBar = view.findViewById(R.id.ratingBarProfile);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        btnEnVenta = view.findViewById(R.id.btnEnVenta);
        btnComprados = view.findViewById(R.id.btnComprados);
        btnBlockedUsers = view.findViewById(R.id.btnBlockedUsers);
        btnBlockedUsers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BlockedUsersActivity.class);
            startActivity(intent);
        });



        recyclerVenta = view.findViewById(R.id.recyclerVenta);
        recyclerComprados = view.findViewById(R.id.recyclerComprados);
        recyclerOffers = view.findViewById(R.id.recyclerOffers);

        Button btnBlockedUsers = view.findViewById(R.id.btnBlockedUsers);

        btnBlockedUsers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BlockedUsersActivity.class);
            startActivity(intent);
        });

        recyclerOffers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Adaptador ofertas
        offersAdapter = new OffersAdapter(offersList, offer -> {
            String productId = (String) offer.get("productId");
            Intent i = new Intent(getActivity(), ProductDetailActivity.class);
            i.putExtra("productId", productId);
            startActivity(i);
        });

        recyclerOffers.setAdapter(offersAdapter);

        // Usuario cargado
        if (user != null) {
            mostrarDatosUsuario(user);
            cargarRatingUsuario(user.getUid());
            cargarProductosVentaYComprados(user.getUid());
            cargarOfertasRecibidas(user.getUid());
        }

        btnEditProfile.setOnClickListener(v -> mostrarOpcionesEditarPerfil());

        btnLogout.setOnClickListener(v -> {

            if (ratingListener != null) ratingListener.remove();
            auth.signOut();

            // Ir a LoginActivity
            Intent i = new Intent(requireActivity(), com.tzolas.camisetaswallapop.activities.LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);

            // Cerrar la Activity actual de forma segura
            requireActivity().finish();
        });


        // Listas de productos
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

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) cargarOfertasRecibidas(user.getUid());
    }

    //DATOS DEL USUARIO
    private void mostrarDatosUsuario(FirebaseUser firebaseUser) {
        tvName.setText(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Sin nombre");
        tvEmail.setText(firebaseUser.getEmail());

        Uri photo = firebaseUser.getPhotoUrl();
        Glide.with(this)
                .load(photo != null ? photo : R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(ivProfilePhoto);

        //  Cargar puntos en tiempo real

        db.collection("users")
                .document(firebaseUser.getUid())
                .addSnapshotListener((doc, error) -> {

                    if (error != null || doc == null || !doc.exists()) return;

                    Long points = doc.getLong("points");
                    Long spentPoints = doc.getLong("spentPoints");

                    TextView tvPoints = getView().findViewById(R.id.tvPoints);

                    if (tvPoints != null) {
                        tvPoints.setText((points != null ? points : 0) + " pts");
                    }

                    // Si quieres mostrar también spentPoints, activa esto:
                    // TextView tvSpent = getView().findViewById(R.id.tvSpentPoints);
                    // if (tvSpent != null) tvSpent.setText("Gastados: " + (spentPoints != null ? spentPoints : 0));
                });
    }


    //ATING EN TIEMPO REAL
    private void cargarRatingUsuario(String uid) {

        if (ratingListener != null) ratingListener.remove();

        ratingListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((doc, error) -> {

                    if (error != null || doc == null || !doc.exists()) return;

                    long sum = doc.getLong("ratingSum") != null ? doc.getLong("ratingSum") : 0;
                    long count = doc.getLong("ratingCount") != null ? doc.getLong("ratingCount") : 0;

                    if (count == 0) {
                        ratingBar.setRating(0);
                        txtRatingCount.setText("(Sin valoraciones)");
                    } else {
                        float avg = (float) sum / count;
                        ratingBar.setRating(avg);
                        txtRatingCount.setText("(" + count + ")");
                    }
                });
    }


    private void cargarProductosVentaYComprados(String uid) {


        db.collection("products")
                .whereEqualTo("userId", uid)
                .whereEqualTo("sold", false)
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

    private void mostrarVenta() {
        recyclerVenta.setVisibility(View.VISIBLE);
        recyclerComprados.setVisibility(View.GONE);
    }

    private void mostrarComprados() {
        recyclerVenta.setVisibility(View.GONE);
        recyclerComprados.setVisibility(View.VISIBLE);
    }

    // FOTO
    private void abrirGaleria() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int r, int res, @Nullable Intent data) {
        super.onActivityResult(r, res, data);

        if (r == PICK_IMAGE && res == Activity.RESULT_OK && data != null) {

            Uri img = data.getData();

            // Muestra la imagen temporalmente
            ivProfilePhoto.setImageURI(img);

            // 🔥 SUBIR foto a Firebase
            subirFotoPerfil(img);
        }
    }


    //EDITAR PERFIL
    private void mostrarOpcionesEditarPerfil() {

        String[] opciones = {"Cambiar foto", "Cambiar nombre"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else mostrarDialogoEditarNombre();
                })
                .show();
    }

    private void mostrarDialogoEditarNombre() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_name, null, false);

        EditText edtName = view.findViewById(R.id.edtNewName);
        edtName.setText(tvName.getText());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Editar nombre")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = edtName.getText().toString().trim();
                    if (!nuevoNombre.isEmpty()) actualizarNombreEnFirebase(nuevoNombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarNombreEnFirebase(String nuevoNombre) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.updateProfile(
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nuevoNombre)
                        .build()
        ).addOnSuccessListener(a -> {

            tvName.setText(nuevoNombre);

            db.collection("users")
                    .document(user.getUid())
                    .update("name", nuevoNombre)
                    .addOnSuccessListener(v ->
                            Toast.makeText(getContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                    );

        });
    }

    // OFERTAS RECIBIDAS
    private void cargarOfertasRecibidas(String sellerId) {

        txtOffersTitle.setVisibility(View.GONE);
        recyclerOffers.setVisibility(View.GONE);

        offersList.clear();
        offersAdapter.notifyDataSetChanged();

        db.collection("products")
                .whereEqualTo("userId", sellerId)
                .get()
                .addOnSuccessListener(productsQuery -> {

                    for (DocumentSnapshot productDoc : productsQuery) {

                        String productId = productDoc.getId();
                        String productTitle = productDoc.getString("title");

                        productDoc.getReference()
                                .collection("offers")
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener(offersQuery -> {

                                    for (DocumentSnapshot offerDoc : offersQuery) {

                                        Map<String, Object> offerData = offerDoc.getData();
                                        if (offerData == null) continue;

                                        offerData.put("productId", productId);
                                        offerData.put("productTitle", productTitle);

                                        String buyerId = offerDoc.getString("buyerId");

                                        db.collection("users")
                                                .document(buyerId)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {

                                                    offerData.put("buyerEmail",
                                                            userDoc.getString("email"));

                                                    offersList.add(offerData);
                                                    offersAdapter.notifyDataSetChanged();

                                                    txtOffersTitle.setVisibility(View.VISIBLE);
                                                    recyclerOffers.setVisibility(View.VISIBLE);
                                                });
                                    }
                                });
                    }

                });
    }
    private void subirFotoPerfil(Uri imageUri) {

        new Thread(() -> {
            try {
                CloudinaryUploader uploader = new CloudinaryUploader();
                String url = uploader.uploadImage(requireContext(), imageUri);

                if (url == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error subiendo foto", Toast.LENGTH_SHORT).show());
                    return;
                }

                String uid = FirebaseAuth.getInstance().getUid();
                if (uid == null) return;

                // 1Guardar en FirebaseAuth
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.updateProfile(
                            new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(url))
                                    .build()
                    );
                }

                // Guardar en Firestore
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("photo", url);

                //Mostrar en pantalla
                requireActivity().runOnUiThread(() -> {
                    Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .into(ivProfilePhoto);

                    Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        }).start();
    }

}
