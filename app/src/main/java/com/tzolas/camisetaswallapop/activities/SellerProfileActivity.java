package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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

public class SellerProfileActivity extends AppCompatActivity {

    private ImageView imgSeller;
    private TextView txtSellerName, txtSellerEmail;
    private RecyclerView recycler;
    private ProductsAdapter adapter;
    private final List<Product> productList = new ArrayList<>();

    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        // UI
        imgSeller = findViewById(R.id.imgSellerProfile);
        txtSellerName = findViewById(R.id.txtSellerNameProfile);
        txtSellerEmail = findViewById(R.id.txtSellerEmailProfile);
        recycler = findViewById(R.id.recyclerSellerProducts);

        // Obtener ID del vendedor
        sellerId = getIntent().getStringExtra("sellerId");

        if (sellerId == null) {
            finish();
            return;
        }

        // Configurar recycler
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductsAdapter(this, productList, product -> {
            // Abrir detalle del producto al pulsarlo
            Intent intent = new Intent(SellerProfileActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        // Cargar datos del vendedor
        loadSellerInfo();
        loadSellerProducts();
    }

    /** ===============================
     * 🔥 Cargar datos del usuario
     * =============================== */
    private void loadSellerInfo() {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(sellerId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

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

    /** ===============================
     * 🔥 Cargar productos del vendedor
     * =============================== */
    private void loadSellerProducts() {

        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("userId", sellerId)
                .get()
                .addOnSuccessListener(query -> {

                    productList.clear();

                    for (var doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            productList.add(p);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
