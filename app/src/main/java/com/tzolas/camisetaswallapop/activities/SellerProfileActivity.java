package com.tzolas.camisetaswallapop.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class SellerProfileActivity extends AppCompatActivity {

    private ImageView imgSeller;
    private TextView txtSellerName, txtSellerEmail;
    private RecyclerView recycler;
    private ProductsAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private RatingBar ratingBar;
    private TextView txtRatingCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        imgSeller = findViewById(R.id.imgSellerProfile);
        txtSellerName = findViewById(R.id.txtSellerNameProfile);
        txtSellerEmail = findViewById(R.id.txtSellerEmailProfile);
        recycler = findViewById(R.id.recyclerSellerProducts);
        ratingBar = findViewById(R.id.ratingBarProfile);
        txtRatingCount = findViewById(R.id.txtRatingCount);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductsAdapter(this, productList, product -> {
            // Qué pasa cuando se hace click en un producto
            // Por ejemplo, abrir detalles del producto
            // (puedes dejarlo vacío si no quieres clics aquí)
        });        recycler.setAdapter(adapter);

        String sellerId = getIntent().getStringExtra("sellerId");

        if (sellerId != null) {
            loadSellerInfo(sellerId);
            loadSellerProducts(sellerId);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(sellerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Double sum = doc.getDouble("ratingSum");
                Long count = doc.getLong("ratingCount");
                double ratingSum = sum != null ? sum : 0.0;
                long ratingCount = count != null ? count : 0;

                double avg = ratingCount > 0 ? ratingSum / ratingCount : 0.0;
                ratingBar.setRating((float) avg);
                txtRatingCount.setText("(" + ratingCount + " valoraciones)");
            }
        });
    }

    private void loadSellerInfo(String sellerId) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(sellerId)
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.getString("name");
                    String photo = doc.getString("photo");
                    String email = doc.getString("email");

                    txtSellerName.setText(name != null ? name : "Vendedor");
                    txtSellerEmail.setText(email != null ? email : "");

                    if (photo != null && !photo.isEmpty()) {
                        Glide.with(this)
                                .load(photo)
                                .circleCrop()
                                .into(imgSeller);
                    } else {
                        imgSeller.setImageResource(R.drawable.ic_user_placeholder);
                    }
                });
    }

    private void loadSellerProducts(String sellerId) {

        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("userId", sellerId)
                .get()
                .addOnSuccessListener(query -> {

                    productList.clear();

                    for (var doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) productList.add(p);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
    private void rateSeller(String sellerId, String buyerId, String orderId, int score) {
        if (score < 1 || score > 5) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1) Evitar valoraciones duplicadas por venta
        db.collection("ratings")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("buyerId", buyerId)
                .whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        // Ya existe valoración para esta venta
                        android.widget.Toast.makeText(this, "Ya valoraste esta venta.", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) Crear rating y actualizar agregados en una transacción
                    db.runTransaction(trx -> {
                        // crear rating
                        DocumentReference ratingRef = db.collection("ratings").document();
                        Map<String, Object> ratingDoc = new HashMap<>();
                        ratingDoc.put("sellerId", sellerId);
                        ratingDoc.put("buyerId", buyerId);
                        ratingDoc.put("orderId", orderId);
                        ratingDoc.put("score", score);
                        ratingDoc.put("timestamp", FieldValue.serverTimestamp());
                        trx.set(ratingRef, ratingDoc);

                        // actualizar user agregados
                        DocumentReference userRef = db.collection("users").document(sellerId);
                        DocumentSnapshot userSnap = trx.get(userRef);

                        double currentSum = 0.0;
                        long currentCount = 0;

                        if (userSnap.exists()) {
                            Double s = userSnap.getDouble("ratingSum");
                            Long c = userSnap.getLong("ratingCount");
                            currentSum = s != null ? s : 0.0;
                            currentCount = c != null ? c : 0;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("ratingSum", currentSum + score);
                        updates.put("ratingCount", currentCount + 1);
                        updates.put("avgRating", (currentSum + score) / (currentCount + 1.0));
                        trx.set(userRef, updates, SetOptions.merge());

                        return null;
                    }).addOnSuccessListener(v -> {
                        android.widget.Toast.makeText(this, "¡Gracias por tu valoración!", android.widget.Toast.LENGTH_SHORT).show();
                        // Opcional: refresca el header del perfil para ver el nuevo promedio
                        reloadSellerRatingHeader();
                    }).addOnFailureListener(e -> {
                        android.widget.Toast.makeText(this, "Error al valorar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Error al comprobar valoraciones: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                });
    }
    private void reloadSellerRatingHeader() {
        if (txtSellerName == null) return; // asegúrate de tener este campo global en la Activity

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(String.valueOf(txtSellerName)).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double sum = doc.getDouble("ratingSum");
                        Long count = doc.getLong("ratingCount");

                        double ratingSum = sum != null ? sum : 0.0;
                        long ratingCount = count != null ? count : 0;
                        double avg = ratingCount > 0 ? ratingSum / ratingCount : 0.0;

                        // ✅ actualiza la UI
                        ratingBar.setRating((float) avg);
                        txtRatingCount.setText(
                                ratingCount == 0 ?
                                        "(Sin valoraciones)" :
                                        String.format(Locale.getDefault(), "(%.1f★ · %d valoraciones)", avg, ratingCount)
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SellerProfile", "Error recargando rating: ", e);
                });
    }

}
