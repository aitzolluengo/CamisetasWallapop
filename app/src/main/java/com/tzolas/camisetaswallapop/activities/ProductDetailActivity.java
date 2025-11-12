package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Chat;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.ChatRepository;
import com.tzolas.camisetaswallapop.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView img, imgSeller;
    private TextView title, price, description, txtSellerName;
    private Button btnChat, btnVender;
    private LinearLayout containerExtra;

    private FirebaseFirestore db;
    private Product currentProduct;
    private String productId; // ← campo real (no lo sombreamos con una local)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // UI
        img = findViewById(R.id.imgDetail);
        title = findViewById(R.id.txtTitleDetail);
        price = findViewById(R.id.txtPriceDetail);
        description = findViewById(R.id.txtDescriptionDetail);
        containerExtra = findViewById(R.id.containerExtra);
        btnChat = findViewById(R.id.btnChat);
        imgSeller = findViewById(R.id.imgSeller);
        txtSellerName = findViewById(R.id.txtSellerName);
        btnVender = findViewById(R.id.btnVender);

        db = FirebaseFirestore.getInstance();

        // Recupera el id del intent y carga
        productId = getIntent().getStringExtra("productId");
        if (productId != null && !productId.trim().isEmpty()) {
            loadProduct(productId);
        } else {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** =========================================================
     * Cargar datos del producto
     * ========================================================= */
    private void loadProduct(String productId) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    Product p = doc.toObject(Product.class);
                    if (p == null) return;

                    // Si en tu modelo no guardas el id dentro, lo seteamos
                    if (p.getId() == null || p.getId().isEmpty()) {
                        p.setId(doc.getId());
                    }

                    currentProduct = p;

                    title.setText(p.getTitle());
                    price.setText(String.format(Locale.getDefault(), "%.2f€", p.getPrice()));
                    description.setText(p.getDescription());

                    Glide.with(this)
                            .load(p.getImageUrl())
                            .placeholder(R.drawable.bg_image_placeholder)
                            .into(img);

                    Map<String, Object> extra = p.getExtra();
                    if (extra != null && !extra.isEmpty()) fillExtraData(extra);

                    loadSellerInfo(p.getUserId());

                    btnChat.setOnClickListener(v -> startChat(p));

                    // Importante: decidir visibilidad del botón Vender
                    updateSellButtonVisibility();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando producto", Toast.LENGTH_SHORT).show()
                );
    }

    /** =========================================================
     * Renderizar EXTRAS
     * ========================================================= */
    private void fillExtraData(Map<String, Object> extra) {
        containerExtra.removeAllViews();

        for (String key : extra.keySet()) {
            Object value = extra.get(key);

            if (value instanceof Long && key.toLowerCase().contains("fecha")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
     * Cargar info del vendedor
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

                    String name = doc.getString("name");
                    String photoUrl = doc.getString("photo");

                    txtSellerName.setText((name != null && !name.trim().isEmpty()) ? name : "Vendedor");

                    if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                        Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(imgSeller);
                    } else {
                        imgSeller.setImageResource(R.drawable.ic_user_placeholder);
                    }

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
     * Crear/abrir chat
     * ========================================================= */
    private void startChat(Product p) {
        String buyerId = FirebaseAuth.getInstance().getUid();
        String sellerId = p.getUserId();
        String pid = p.getId();
        if (buyerId == null) return;

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, pid)
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String chatId = query.getDocuments().get(0).getId();
                        openChat(chatId, sellerId, pid);
                        return;
                    }

                    String chatId = repo.generateId();

                    // Crea el doc del chat con lista de participantes
                    db.collection("chats")
                            .document(chatId)
                            .set(new HashMap<String, Object>() {{
                                put("id", chatId);
                                put("user1", buyerId);
                                put("user2", sellerId);
                                put("productId", pid);
                                put("createdAt", System.currentTimeMillis());
                                put("participants", Arrays.asList(buyerId, sellerId));
                            }})
                            .addOnSuccessListener(v -> openChat(chatId, sellerId, pid));
                });
    }

    private void openChat(String chatId, String sellerId, String productId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chatId", chatId);
        i.putExtra("sellerId", sellerId);
        i.putExtra("productId", productId);
        startActivity(i);
    }

    /** =========================================================
     * Botón VENDER
     * ========================================================= */
    private void updateSellButtonVisibility() {
        String uid = FirebaseAuth.getInstance().getUid();
        boolean show = currentProduct != null
                && uid != null
                && uid.equals(currentProduct.getUserId())
                && !currentProduct.isSold();
        btnVender.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            btnVender.setOnClickListener(v -> showSellDialog());
        }
    }

    private void showSellDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_sell_product, null, false);
        EditText edt = v.findViewById(R.id.edtBuyerEmail);

        new AlertDialog.Builder(this)
                .setTitle("Vender producto")
                .setView(v)
                .setPositiveButton("Confirmar", (d, w) -> {
                    String email = edt.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Introduce el email del comprador", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    findBuyerAndSell(email);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void findBuyerAndSell(String buyerEmail) {
        // Busca el comprador por email en colección users
        db.collection("users")
                .whereEqualTo("email", buyerEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "No existe un usuario con ese email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String buyerId = snap.getDocuments().get(0).getId();
                    createOrderAndMarkSold(buyerId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error buscando comprador: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void createOrderAndMarkSold(String buyerId) {
        if (currentProduct == null) return;
        String sellerId = FirebaseAuth.getInstance().getUid();
        if (sellerId == null) return;

        DocumentReference prodRef = db.collection("products").document(currentProduct.getId());
        DocumentReference orderRef = db.collection("orders").document(); // nueva orden

        db.runTransaction(trx -> {
            // 1) Orden
            Map<String, Object> order = new HashMap<>();
            order.put("id", orderRef.getId());
            order.put("productId", currentProduct.getId());
            order.put("sellerId", sellerId);
            order.put("buyerId", buyerId);
            order.put("price", currentProduct.getPrice());
            order.put("status", "completed"); // o "pending"
            order.put("timestamp", FieldValue.serverTimestamp());
            trx.set(orderRef, order);

            // 2) Producto vendido
            Map<String, Object> updates = new HashMap<>();
            updates.put("sold", true);
            updates.put("buyerId", buyerId);
            updates.put("orderId", orderRef.getId());
            updates.put("soldAt", System.currentTimeMillis());
            trx.set(prodRef, updates, SetOptions.merge());

            return null;
        }).addOnSuccessListener(v -> {
            Toast.makeText(this, "¡Producto vendido!", Toast.LENGTH_SHORT).show();
            currentProduct.setSold(true);
            currentProduct.setBuyerId(buyerId);
            btnVender.setVisibility(View.GONE);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al vender: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
