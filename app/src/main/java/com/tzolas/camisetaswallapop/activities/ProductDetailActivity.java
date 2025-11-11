package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Chat;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.ChatRepository;
import com.tzolas.camisetaswallapop.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView img, imgSeller;
    private TextView title, price, description, txtSellerName;
    private Button btnChat;
    private LinearLayout containerExtra;

    private Product currentProduct;   // ✅ guardamos referencia

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        img = findViewById(R.id.imgDetail);
        title = findViewById(R.id.txtTitleDetail);
        price = findViewById(R.id.txtPriceDetail);
        description = findViewById(R.id.txtDescriptionDetail);
        containerExtra = findViewById(R.id.containerExtra);
        btnChat = findViewById(R.id.btnChat);
        imgSeller = findViewById(R.id.imgSeller);
        txtSellerName = findViewById(R.id.txtSellerName);

        String productId = getIntent().getStringExtra("productId");
        if (productId != null) loadProduct(productId);
    }

    /** =========================================================
     * ✅ Cargar datos del producto
     * ========================================================= */
    private void loadProduct(String productId) {

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {

                    Product p = doc.toObject(Product.class);
                    if (p == null) return;

                    currentProduct = p;

                    title.setText(p.getTitle());
                    price.setText(p.getPrice() + "€");
                    description.setText(p.getDescription());

                    Glide.with(this)
                            .load(p.getImageUrl())
                            .placeholder(R.drawable.bg_image_placeholder)
                            .into(img);

                    // extras dinámicos
                    Map<String, Object> extra = p.getExtra();
                    if (extra != null && !extra.isEmpty()) {
                        fillExtraData(extra);
                    }

                    // info usuario
                    loadSellerInfo(p.getUserId());

                    // ✅ CHAT
                    btnChat.setOnClickListener(v -> startChat(p));
                });
    }

    /** =========================================================
     * ✅ Renderizar EXTRAS
     * ========================================================= */
    private void fillExtraData(Map<String, Object> extra) {
        containerExtra.removeAllViews();

        for (String key : extra.keySet()) {

            Object value = extra.get(key);

            // ✅ Formato fecha si corresponde
            if (value instanceof Long && key.toLowerCase().contains("fecha")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                value = sdf.format(new Date((Long) value));
            }

            String prettyKey = key.substring(0, 1).toUpperCase() + key.substring(1);

            TextView tv = new TextView(this);
            tv.setText("• " + prettyKey + ": " + value);
            tv.setTextSize(15);
            tv.setTextColor(0xFF444444);

            containerExtra.addView(tv);
        }
    }


    /** =========================================================
     * ✅ Cargar info del vendedor
     * ========================================================= */
    private void loadSellerInfo(String sellerId) {

        if (sellerId == null || sellerId.trim().isEmpty()) return;

        new UserRepository().getUserById(sellerId)
                .addOnSuccessListener(doc -> {

                    if (doc == null || !doc.exists()) {
                        txtSellerName.setText("Vendedor");
                        imgSeller.setImageResource(R.drawable.ic_user_placeholder);
                        return;
                    }

                    String name     = doc.getString("name");
                    String photoUrl = doc.getString("photo");

                    txtSellerName.setText(
                            (name != null && !name.trim().isEmpty())
                                    ? name
                                    : "Vendedor"
                    );

                    if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                        Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(imgSeller);
                    } else {
                        imgSeller.setImageResource(R.drawable.ic_user_placeholder);
                    }

                    // ✅ Pulsar el bloque → abrir perfil
                    findViewById(R.id.sellerBlock).setOnClickListener(v -> {
                        Intent i = new Intent(ProductDetailActivity.this, SellerProfileActivity.class);
                        i.putExtra("sellerId", sellerId);
                        startActivity(i);
                    });

                })
                .addOnFailureListener(e -> {
                    txtSellerName.setText("Vendedor");
                    imgSeller.setImageResource(R.drawable.ic_user_placeholder);
                });
    }


    /** =========================================================
     * ✅ CREAR / ABRIR CHAT
     * ========================================================= */
    private void startChat(Product p) {

        String buyerId  = FirebaseAuth.getInstance().getUid();
        String sellerId = p.getUserId();
        String productId = p.getId();

        if (buyerId == null) return; // no logeado

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, productId)
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {
                        // ✅ ya existe chat
                        String chatId = query.getDocuments().get(0).getId();
                        openChat(chatId, sellerId, productId);
                        return;
                    }

                    // ✅ crear chat
                    String chatId = repo.generateId();

                    Chat chat = new Chat(
                            chatId,
                            buyerId,
                            sellerId,
                            productId,
                            System.currentTimeMillis()
                    );

                    repo.createChat(chat)
                            .addOnSuccessListener(v -> openChat(chatId, sellerId, productId));
                });
    }


    /** =========================================================
     * ✅ Ir a ChatActivity
     * ========================================================= */
    private void openChat(String chatId, String sellerId, String productId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chatId", chatId);
        i.putExtra("sellerId", sellerId);
        i.putExtra("productId", productId);
        startActivity(i);
    }

}
