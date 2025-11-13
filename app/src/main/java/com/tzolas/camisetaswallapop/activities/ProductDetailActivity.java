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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Button btnChat, btnVender, btnEliminar;
    private LinearLayout containerExtra;

    private FirebaseFirestore db;
    private Product currentProduct;
    private String productId;

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
        btnEliminar = findViewById(R.id.btnEliminar);

        db = FirebaseFirestore.getInstance();

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

                    // Lógica de dueño / comprador
                    setupOwnerOrBuyerUI();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando producto", Toast.LENGTH_SHORT).show()
                );
    }

    /** Decide si mostrar chat o editar/eliminar según si es tuyo */
    private void setupOwnerOrBuyerUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = (user != null) ? user.getUid() : null;

        boolean isOwner = myUid != null && currentProduct.getUserId().equals(myUid);

        if (isOwner) {
            // Soy el dueño → NO chatear conmigo mismo, SÍ editar/eliminar
            btnChat.setVisibility(View.GONE);
            findViewById(R.id.sellerBlock).setVisibility(View.GONE);

            btnVender.setVisibility(View.VISIBLE);
            btnVender.setText("Editar producto");
            btnVender.setOnClickListener(v -> abrirDialogoEditarProducto());

            btnEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setOnClickListener(v -> confirmarEliminarProducto());

        } else {
            // No soy el dueño → ver vendedor y chatear
            btnChat.setVisibility(View.VISIBLE);
            findViewById(R.id.sellerBlock).setVisibility(View.VISIBLE);

            btnVender.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);

            btnChat.setOnClickListener(v -> startChat(currentProduct));
        }
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
     * EDITAR PRODUCTO (solo dueño)
     * ========================================================= */
    private void abrirDialogoEditarProducto() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);

        EditText edtTitle = view.findViewById(R.id.edtEditTitle);
        EditText edtPrice = view.findViewById(R.id.edtEditPrice);
        EditText edtDesc = view.findViewById(R.id.edtEditDescription);

        edtTitle.setText(currentProduct.getTitle());
        edtPrice.setText(String.valueOf(currentProduct.getPrice()));
        edtDesc.setText(currentProduct.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Editar producto")
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {
                    String t = edtTitle.getText().toString().trim();
                    String pr = edtPrice.getText().toString().trim();
                    String desc = edtDesc.getText().toString().trim();
                    guardarEdicionProducto(t, pr, desc);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarEdicionProducto(String newTitle, String newPrice, String newDesc) {
        if (currentProduct == null) return;

        double priceValue;
        try {
            priceValue = Double.parseDouble(newPrice);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("price", priceValue);
        updates.put("description", newDesc);

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(currentProduct.getId())
                .update(updates)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    title.setText(newTitle);
                    description.setText(newDesc);
                    price.setText(String.format(Locale.getDefault(), "%.2f€", priceValue));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /** =========================================================
     * ELIMINAR PRODUCTO (solo dueño)
     * ========================================================= */
    private void confirmarEliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que quieres eliminar este producto?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto() {
        if (currentProduct == null) return;

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(currentProduct.getId())
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
