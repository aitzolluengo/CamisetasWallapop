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
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorLayout;

    private TextView title, price, description, txtSellerName;
    private View sellerBlock;
    private Button btnChat, btnVender, btnEliminar, btnOferta, btnComprar;

    private LinearLayout containerExtra;
    private FirebaseFirestore db;

    private Product currentProduct;
    private String productId;

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

        sellerBlock = findViewById(R.id.sellerBlock);

        btnChat = findViewById(R.id.btnChat);
        btnVender = findViewById(R.id.btnVender);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnOferta = findViewById(R.id.btnOferta);
        btnComprar = findViewById(R.id.btnComprar);

        containerExtra = findViewById(R.id.containerExtra);

        db = FirebaseFirestore.getInstance();
        productId = getIntent().getStringExtra("productId");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(productId);
    }

    private void loadProduct(String productId) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {

                    Product p = doc.toObject(Product.class);
                    if (p == null) return;

                    p.setId(doc.getId());
                    currentProduct = p;

                    title.setText(p.getTitle());
                    price.setText(String.format(Locale.getDefault(), "%.0f puntos", p.getPrice()));
                    description.setText(p.getDescription());

                    setupImageSlider(p.getImageUrls());
                    fillExtraData(p.getExtra());
                    loadSellerInfo(p.getUserId());
                    setupOwnerOrBuyerUI();
                    mostrarValoracionSiSoyComprador();
                });
    }

    private void setupImageSlider(java.util.List<String> urls) {
        if (urls == null || urls.isEmpty()) return;

        DetailImageAdapter adapter = new DetailImageAdapter(this, urls);
        viewPagerImages.setAdapter(adapter);

        setupIndicators(urls.size());
        setCurrentIndicator(0);

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int pos) {
                setCurrentIndicator(pos);
            }
        });
    }

    private void setupIndicators(int count) {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.indicador_inactive);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            params.setMargins(8, 0, 8, 0);
            indicatorLayout.addView(dot, params);
        }
    }

    private void setCurrentIndicator(int index) {
        int total = indicatorLayout.getChildCount();
        for (int i = 0; i < total; i++) {
            ImageView dot = (ImageView) indicatorLayout.getChildAt(i);
            dot.setImageResource(i == index
                    ? R.drawable.indicador_active
                    : R.drawable.indicador_inactive);
        }
    }

    private void setupOwnerOrBuyerUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : null;

        boolean isOwner = uid != null && currentProduct.getUserId().equals(uid);

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

    private void fillExtraData(Map<String, Object> extra) {
        if (extra == null) return;

        containerExtra.removeAllViews();

        for (String key : extra.keySet()) {
            Object value = extra.get(key);

            if (value instanceof Long && key.toLowerCase().contains("fecha")) {
                value = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date((Long) value));
            }

            TextView tv = new TextView(this);
            tv.setText("• " + key + ": " + value);
            tv.setTextSize(15);
            tv.setTextColor(0xFF444444);
            containerExtra.addView(tv);
        }
    }

    private void loadSellerInfo(String sellerId) {
        new UserRepository()
                .getUserById(sellerId)
                .addOnSuccessListener(doc ->
                        txtSellerName.setText(doc.getString("name"))
                );
    }

    // ===========================
    //     CONTACTAR VENDEDOR
    // ===========================
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

                    db.collection("chats")
                            .document(chatId)
                            .set(data)
                            .addOnSuccessListener(v -> openChat(chatId, sellerId, p.getId()));
                });
    }

    private void openChat(String chatId, String sellerId, String productId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("chatId", chatId);
        i.putExtra("sellerId", sellerId);
        i.putExtra("productId", productId);
        startActivity(i);
    }

    // ===========================
    //       ENVIAR OFERTA
    // ===========================
    private void enviarOferta() {
        String buyerId = FirebaseAuth.getInstance().getUid();
        String sellerId = currentProduct.getUserId();

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, currentProduct.getId())
                .addOnSuccessListener(q -> {

                    if (!q.isEmpty()) {
                        abrirDialogoEnviarOferta(
                                q.getDocuments().get(0).getId(),
                                buyerId, sellerId
                        );
                        return;
                    }

                    String chatId = repo.generateId();

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", chatId);
                    data.put("user1", buyerId);
                    data.put("user2", sellerId);
                    data.put("productId", currentProduct.getId());
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("participants", Arrays.asList(buyerId, sellerId));

                    db.collection("chats").document(chatId)
                            .set(data)
                            .addOnSuccessListener(v ->
                                    abrirDialogoEnviarOferta(chatId, buyerId, sellerId)
                            );
                });
    }

    private void abrirDialogoEnviarOferta(String chatId, String buyerId, String sellerId) {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_offer_input, null);

        EditText edtOffer = view.findViewById(R.id.edtOfferPrice);

        new AlertDialog.Builder(this)
                .setTitle("Enviar oferta")
                .setView(view)
                .setPositiveButton("Enviar", (dialog, which) -> {

                    String t = edtOffer.getText().toString().trim();
                    if (t.isEmpty()) {
                        Toast.makeText(this, "Introduce una cantidad", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int price;
                    try {
                        price = Integer.parseInt(t);
                    } catch (Exception ex) {
                        Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new com.tzolas.camisetaswallapop.repositories.OrderRepository()
                            .sendOffer(currentProduct.getId(),chatId, buyerId, price)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Oferta enviada", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e -> {
                                Log.e("FIRESTORE_ERROR", "Error enviando oferta", e);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ===========================
    //     VALORAR VENDEDOR
    // ===========================
    private void mostrarValoracionSiSoyComprador() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        if (!currentProduct.isSold()) return;
        if (!uid.equals(currentProduct.getBuyerId())) return;

        String ratingId = currentProduct.getId() + "_" + uid;

        db.collection("ratings")
                .document(ratingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        mostrarDialogoValoracion(currentProduct.getUserId());
                });
    }

    private void mostrarDialogoValoracion(String sellerId) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_rating, null);
        android.widget.RatingBar ratingBar =
                view.findViewById(R.id.ratingBarVote);

        new AlertDialog.Builder(this)
                .setTitle("Valorar vendedor")
                .setView(view)
                .setPositiveButton("Enviar", (d, w) -> {
                    float stars = ratingBar.getRating();
                    if (stars <= 0) return;

                    guardarValoracion(sellerId, stars);
                })
                .show();
    }

    private void guardarValoracion(String sellerId, float stars) {
        String buyerId = FirebaseAuth.getInstance().getUid();
        String ratingId = currentProduct.getId() + "_" + buyerId;

        Map<String, Object> rating = new HashMap<>();
        rating.put("sellerId", sellerId);
        rating.put("buyerId", buyerId);
        rating.put("productId", currentProduct.getId());
        rating.put("stars", stars);
        rating.put("timestamp", System.currentTimeMillis());

        db.collection("ratings")
                .document(ratingId)
                .set(rating)
                .addOnSuccessListener(v -> db.collection("users")
                        .document(sellerId)
                        .update(
                                "ratingSum", FieldValue.increment((int) stars),
                                "ratingCount", FieldValue.increment(1)
                        ));
    }

    // ===========================
    //   EDITAR / ELIMINAR PRODUCTO
    // ===========================
    private void abrirDialogoEditarProducto() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_product, null);

        EditText t = view.findViewById(R.id.edtEditTitle);
        EditText p = view.findViewById(R.id.edtEditPrice);
        EditText d = view.findViewById(R.id.edtEditDescription);

        t.setText(currentProduct.getTitle());
        p.setText(String.valueOf(currentProduct.getPrice()));
        d.setText(currentProduct.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Editar producto")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, w) ->
                        guardarEdicionProducto(
                                t.getText().toString(),
                                p.getText().toString(),
                                d.getText().toString()
                        ))
                .show();
    }

    private void guardarEdicionProducto(String newTitle, String newPrice, String newDesc) {
        double priceValue;
        try { priceValue = Double.parseDouble(newPrice); }
        catch (Exception e) {
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
                    title.setText(newTitle);
                    price.setText(String.format(Locale.getDefault(), "%.0f puntos", priceValue));
                    description.setText(newDesc);
                });
    }

    private void confirmarEliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro?")
                .setPositiveButton("Sí", (d, w) -> eliminarProducto())
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarProducto() {
        db.collection("products")
                .document(currentProduct.getId())
                .delete()
                .addOnSuccessListener(v -> finish());
    }

    // ===========================
    //        COMPRAR DIRECTO
    // ===========================
    private void comprarProducto() {
        String buyerId = FirebaseAuth.getInstance().getUid();

        new UserRepository()
                .getUserById(buyerId)
                .addOnSuccessListener(doc -> {

                    int points = doc.contains("points") ? doc.getLong("points").intValue() : 0;
                    int price = (int) Math.round(currentProduct.getPrice());

                    if (points < price) {
                        Toast.makeText(this, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Comprar producto")
                            .setMessage("Vas a gastar " + price + " puntos. ¿Confirmar?")
                            .setPositiveButton("Comprar", (d, w) -> {

                                new com.tzolas.camisetaswallapop.repositories.OrderRepository()
                                        .buyProduct(currentProduct.getId(), buyerId)
                                        .addOnSuccessListener(aVoid -> {

                                            Intent i = new Intent(this, ShippingActivity.class);
                                            i.putExtra("productId", currentProduct.getId());
                                            startActivity(i);

                                            currentProduct.setSold(true);
                                            setupOwnerOrBuyerUI();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FIRESTORE_ERROR", "Error comprando", e);
                                            Toast.makeText(this,
                                                    "Error: " + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                });
    }
}