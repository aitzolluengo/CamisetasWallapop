package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.DetailImageAdapter;
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

    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorLayout;

    private TextView title, price, description, txtSellerName, txtOfferAlert;
    private View sellerBlock;
    private Button btnChat, btnVender, btnEliminar, btnOferta, btnComprar;

    private LinearLayout containerExtra;

    private FirebaseFirestore db;
    private Product currentProduct;
    private String productId;

    @Override
    protected void onResume() {
        super.onResume();
        if (productId != null) {
            loadProduct(productId); // recarga después de ShippingActivity
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        title = findViewById(R.id.txtTitleDetail);
        price = findViewById(R.id.txtPriceDetail);
        description = findViewById(R.id.txtDescriptionDetail);
        txtSellerName = findViewById(R.id.txtSellerName);
        txtOfferAlert = findViewById(R.id.txtOfferAlert);

        sellerBlock = findViewById(R.id.sellerBlock);

        btnChat = findViewById(R.id.btnChat);
        btnVender = findViewById(R.id.btnVender);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnOferta = findViewById(R.id.btnOferta);
        btnComprar = findViewById(R.id.btnComprar);

        containerExtra = findViewById(R.id.containerExtra);

        db = FirebaseFirestore.getInstance();
        productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(productId);
    }

    // ============================================================
    // CARGAR PRODUCTO
    // ============================================================
    private void loadProduct(String productId) {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(doc -> {

                    Product p = doc.toObject(Product.class);
                    if (p == null) return;

                    p.setId(doc.getId());
                    currentProduct = p;

                    title.setText(p.getTitle());
                    price.setText(String.format("%.0f puntos", p.getPrice()));
                    description.setText(p.getDescription());

                    setupImageSlider(p.getImageUrls());
                    fillExtraData(p.getExtra());
                    loadSellerInfo(p.getUserId());
                    setupOwnerOrBuyerUI();
                    mostrarValoracionSiSoyComprador();
                });
    }

    // ============================================================
    // SLIDER IMÁGENES
    // ============================================================
    private void setupImageSlider(java.util.List<String> urls) {
        if (urls == null || urls.isEmpty()) return;

        DetailImageAdapter adapter = new DetailImageAdapter(this, urls);
        viewPagerImages.setAdapter(adapter);

        setupIndicators(urls.size());
        setCurrentIndicator(0);

        viewPagerImages.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int pos) {
                        setCurrentIndicator(pos);
                    }
                }
        );
    }

    private void setupIndicators(int count) {
        indicatorLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.indicador_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicatorLayout.addView(dot, params);
        }
    }

    private void setCurrentIndicator(int index) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView dot = (ImageView) indicatorLayout.getChildAt(i);
            dot.setImageResource(i == index ?
                    R.drawable.indicador_active :
                    R.drawable.indicador_inactive);
        }
    }

    // ============================================================
    // UI DUEÑO / COMPRADOR
    // ============================================================
    private void setupOwnerOrBuyerUI() {
        String uid = FirebaseAuth.getInstance().getUid();

        boolean isOwner = uid != null && uid.equals(currentProduct.getUserId());

        if (isOwner) {
            btnChat.setVisibility(View.GONE);
            btnOferta.setVisibility(View.GONE);
            btnComprar.setVisibility(View.GONE);

            btnVender.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.VISIBLE);
            sellerBlock.setVisibility(View.GONE);

            btnVender.setOnClickListener(v -> abrirDialogoEditarProducto());
            btnEliminar.setOnClickListener(v -> confirmarEliminarProducto());

        } else {
            btnChat.setVisibility(View.VISIBLE);
            btnOferta.setVisibility(View.VISIBLE);
            btnComprar.setVisibility(View.VISIBLE);
            sellerBlock.setVisibility(View.VISIBLE);

            btnVender.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);

            btnChat.setOnClickListener(v -> startChat(currentProduct));
            btnOferta.setOnClickListener(v -> enviarOferta());
            btnComprar.setOnClickListener(v -> comprarProducto());
        }
    }

    // ============================================================
    // ALERTA OFERTA PENDIENTE
    // ============================================================

    // ============================================================
    // EXTRA INFO
    // ============================================================
    private void fillExtraData(Map<String, Object> extra) {
        if (extra == null) return;

        containerExtra.removeAllViews();

        for (String key : extra.keySet()) {
            Object v = extra.get(key);

            if (v instanceof Long && key.toLowerCase().contains("fecha")) {
                v = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date((Long) v));
            }

            TextView tv = new TextView(this);
            tv.setText("• " + key + ": " + v);
            tv.setTextSize(15);
            tv.setTextColor(0xFF444444);

            containerExtra.addView(tv);
        }
    }

    // ============================================================
    // VENDEDOR
    // ============================================================
    private void loadSellerInfo(String sellerId) {
        new UserRepository()
                .getUserById(sellerId)
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    txtSellerName.setText(name != null ? name : "Vendedor");

                    // 🔥 SOLO hacer clickable el bloque del vendedor
                    sellerBlock.setOnClickListener(v -> {
                        // Verificar que no sea el propio usuario
                        String currentUserId = FirebaseAuth.getInstance().getUid();
                        if (currentUserId != null && !currentUserId.equals(sellerId)) {
                            abrirPerfilVendedor(sellerId, name);
                        }
                    });
                });
    }

    // ============================================================
    // CHAT
    // ============================================================
    private void startChat(Product p) {
        String buyerId = FirebaseAuth.getInstance().getUid();
        String sellerId = p.getUserId();

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, p.getId())
                .addOnSuccessListener(q -> {

                    if (!q.isEmpty()) {
                        openChat(q.getDocuments().get(0).getId(), sellerId, p.getId());
                        return;
                    }

                    String chatId = repo.generateId();

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", chatId);
                    data.put("user1", buyerId);
                    data.put("user2", sellerId);
                    data.put("productId", p.getId());
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("participants", Arrays.asList(buyerId, sellerId));

                    db.collection("chats").document(chatId)
                            .set(data)
                            .addOnSuccessListener(v ->
                                    openChat(chatId, sellerId, p.getId())
                            );
                });
    }

    private void openChat(String chatId, String sellerId, String productId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chatId", chatId);
        i.putExtra("sellerId", sellerId);
        i.putExtra("productId", productId);
        startActivity(i);
    }

    // ============================================================
    // OFERTA
    // ============================================================
    private void enviarOferta() {

        String buyerId = FirebaseAuth.getInstance().getUid();
        String sellerId = currentProduct.getUserId();
        String productId = currentProduct.getId();

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, productId)
                .addOnSuccessListener(q -> {

                    String chatId;

                    if (!q.isEmpty()) {
                        chatId = q.getDocuments().get(0).getId();
                    } else {
                        chatId = repo.generateId();

                        Map<String, Object> data = new HashMap<>();
                        data.put("id", chatId);
                        data.put("user1", buyerId);
                        data.put("user2", sellerId);
                        data.put("productId", productId);
                        data.put("createdAt", System.currentTimeMillis());
                        data.put("participants", Arrays.asList(buyerId, sellerId));

                        db.collection("chats").document(chatId).set(data);
                    }

                    // 👇 Pasamos sellerId y productId
                    abrirDialogoEnviarOferta(chatId, buyerId, sellerId, productId);
                });
    }


    private void abrirDialogoEnviarOferta(String chatId, String buyerId, String sellerId, String productId) {

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_offer_input, null);
        EditText edtOffer = v.findViewById(R.id.edtOfferPrice);

        new AlertDialog.Builder(this)
                .setTitle("Enviar oferta")
                .setView(v)
                .setPositiveButton("Enviar", (d, w) -> {

                    String t = edtOffer.getText().toString().trim();
                    if (t.isEmpty()) {
                        Toast.makeText(this, "Introduce una cantidad", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount;
                    try { amount = Integer.parseInt(t); }
                    catch (Exception e) {
                        Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 🔥 Crear mensaje tipo oferta en el chat
                    String msgId = java.util.UUID.randomUUID().toString();

                    Map<String, Object> offerMsg = new HashMap<>();
                    offerMsg.put("id", msgId);
                    offerMsg.put("senderId", buyerId);
                    offerMsg.put("timestamp", System.currentTimeMillis());
                    offerMsg.put("type", "offer");
                    offerMsg.put("offerPrice", amount);
                    offerMsg.put("status", "pending");
                    offerMsg.put("delivered", false);
                    offerMsg.put("read", false);

                    db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document(msgId)
                            .set(offerMsg)
                            .addOnSuccessListener(aVoid -> {

                                Toast.makeText(this, "Oferta enviada", Toast.LENGTH_SHORT).show();

                                // Abre el chat automáticamente
                                openChat(chatId, sellerId, productId);

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ============================================================
    // VALORACIÓN
    // ============================================================
    private void mostrarValoracionSiSoyComprador() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || currentProduct == null) return;

        if (!currentProduct.isSold()) return;
        if (currentProduct.getBuyerId() == null) return;
        if (!uid.equals(currentProduct.getBuyerId())) return;

        String ratingId = currentProduct.getId() + "_" + uid;

        db.collection("ratings")
                .document(ratingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        mostrarDialogoValoracion(currentProduct.getUserId());
                    }
                });
    }

    private void mostrarDialogoValoracion(String sellerId) {

        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_rating, null);

        RatingBar rb = v.findViewById(R.id.ratingBarVote);

        new AlertDialog.Builder(this)
                .setTitle("Valorar vendedor")
                .setView(v)
                .setPositiveButton("Enviar", (d, w) -> {

                    float stars = rb.getRating();
                    if (stars > 0) guardarValoracion(sellerId, stars);

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarValoracion(String sellerId, float stars) {

        String buyerId = FirebaseAuth.getInstance().getUid();
        if (buyerId == null) return;

        String id = currentProduct.getId() + "_" + buyerId;

        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", sellerId);
        data.put("buyerId", buyerId);
        data.put("productId", currentProduct.getId());
        data.put("stars", stars);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("ratings").document(id)
                .set(data)
                .addOnSuccessListener(v -> {

                    db.collection("users").document(sellerId)
                            .update(
                                    "ratingSum", FieldValue.increment((int) stars),
                                    "ratingCount", FieldValue.increment(1)
                            );

                    Toast.makeText(this, "¡Gracias por valorar!", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // COMPRAR DIRECTO
    // ============================================================
    private void comprarProducto() {

        String buyerId = FirebaseAuth.getInstance().getUid();

        new UserRepository()
                .getUserById(buyerId)
                .addOnSuccessListener(doc -> {

                    int points = doc.contains("points") ? doc.getLong("points").intValue() : 0;
                    int cost = (int) Math.round(currentProduct.getPrice());

                    if (points < cost) {
                        Toast.makeText(this, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Comprar")
                            .setMessage("Gastarás " + cost + " puntos. ¿Continuar?")
                            .setPositiveButton("Sí", (d, w) -> {

                                new com.tzolas.camisetaswallapop.repositories.OrderRepository()
                                        .buyProduct(currentProduct.getId(), buyerId)
                                        .addOnSuccessListener(aVoid -> {

                                            Intent i = new Intent(this, ShippingActivity.class);
                                            i.putExtra("productId", currentProduct.getId());
                                            startActivity(i);

                                            currentProduct.setSold(true);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show());
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                });
    }
    // ===========================
//   EDITAR / ELIMINAR PRODUCTO
// ===========================
    private void abrirDialogoEditarProducto() {
        if (currentProduct == null) return;

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_product, null);

        EditText edtTitle = view.findViewById(R.id.edtEditTitle);
        EditText edtPrice = view.findViewById(R.id.edtEditPrice);
        EditText edtDesc  = view.findViewById(R.id.edtEditDescription);

        edtTitle.setText(currentProduct.getTitle());
        edtPrice.setText(String.valueOf(currentProduct.getPrice()));
        edtDesc.setText(currentProduct.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Editar producto")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String newTitle = edtTitle.getText().toString().trim();
                    String newPrice = edtPrice.getText().toString().trim();
                    String newDesc  = edtDesc.getText().toString().trim();
                    guardarEdicionProducto(newTitle, newPrice, newDesc);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarEdicionProducto(String newTitle, String newPrice, String newDesc) {
        double priceValue;
        try {
            priceValue = Double.parseDouble(newPrice);
        } catch (Exception e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> up = new HashMap<>();
        up.put("title", newTitle);
        up.put("price", priceValue);
        up.put("description", newDesc);

        db.collection("products")
                .document(currentProduct.getId())
                .update(up)
                .addOnSuccessListener(v -> {
                    // actualizar UI
                    title.setText(newTitle);
                    price.setText(String.format(Locale.getDefault(), "%.0f puntos", priceValue));
                    description.setText(newDesc);
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmarEliminarProducto() {
        if (currentProduct == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que quieres eliminar este producto?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto() {
        db.collection("products")
                .document(currentProduct.getId())
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    // 🔥 METODO SIMPLE: Abrir perfil del vendedor
    private void abrirPerfilVendedor(String sellerId, String sellerName) {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        intent.putExtra("sellerId", sellerId);
        intent.putExtra("sellerName", sellerName);
        startActivity(intent);
    }

}
