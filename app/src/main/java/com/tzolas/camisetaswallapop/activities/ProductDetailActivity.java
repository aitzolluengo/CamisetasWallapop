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

    private TextView title, price, description, txtSellerName, txtOfferAlert;
    private View sellerBlock;
    private Button btnChat, btnVender, btnEliminar, btnOferta;

    private LinearLayout containerExtra;

    private FirebaseFirestore db;
    private Product currentProduct;
    private String productId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // ViewPager
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        // UI
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

        containerExtra = findViewById(R.id.containerExtra);

        db = FirebaseFirestore.getInstance();

        // Recupera ID del producto
        productId = getIntent().getStringExtra("productId");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(productId);
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

                    if (p == null) {
                        finish();
                        return;
                    }

                    if (p.getId() == null) p.setId(doc.getId());
                    currentProduct = p;

                    title.setText(p.getTitle());
                    price.setText(String.format(Locale.getDefault(), "%.2f€", p.getPrice()));
                    description.setText(p.getDescription());

                    setupImageSlider(p.getImageUrls());
                    fillExtraData(p.getExtra());
                    loadSellerInfo(p.getUserId());
                    setupOwnerOrBuyerUI();
                    mostrarValoracionSiSoyComprador();
                    comprobarOfertasPendientes();
                });
    }

    /** =========================================================
     * MULTI-FOTO
     * ========================================================= */
    private void setupImageSlider(java.util.List<String> urls) {
        if (urls == null || urls.isEmpty()) return;

        DetailImageAdapter adapter = new DetailImageAdapter(this, urls);
        viewPagerImages.setAdapter(adapter);

        setupIndicators(urls.size());
        setCurrentIndicator(0);

        viewPagerImages.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        setCurrentIndicator(position);
                    }
                }
        );
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
        int childCount = indicatorLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ImageView dot = (ImageView) indicatorLayout.getChildAt(i);
            dot.setImageResource(i == index
                    ? R.drawable.indicador_active
                    : R.drawable.indicador_inactive);
        }
    }

    /** =========================================================
     * UI: PROPIETARIO vs COMPRADOR
     * ========================================================= */
    private void setupOwnerOrBuyerUI() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = (user != null) ? user.getUid() : null;

        boolean isOwner = myUid != null && currentProduct.getUserId().equals(myUid);

        if (isOwner) {

            btnChat.setVisibility(View.GONE);
            sellerBlock.setVisibility(View.GONE);

            btnVender.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.VISIBLE);
            btnOferta.setVisibility(View.GONE);

            btnVender.setText("Editar producto");
            btnVender.setOnClickListener(v -> abrirDialogoEditarProducto());
            btnEliminar.setOnClickListener(v -> confirmarEliminarProducto());

        } else {

            btnChat.setVisibility(View.VISIBLE);
            sellerBlock.setVisibility(View.VISIBLE);

            btnVender.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
            btnOferta.setVisibility(View.VISIBLE);

            btnChat.setOnClickListener(v -> startChat(currentProduct));
            btnOferta.setOnClickListener(v -> enviarOferta());
        }
    }

    /** =========================================================
     * EXTRA INFO
     * ========================================================= */
    private void fillExtraData(Map<String, Object> extra) {
        if (extra == null) return;

        containerExtra.removeAllViews();

        for (String key : extra.keySet()) {
            Object value = extra.get(key);

            if (value instanceof Long && key.toLowerCase().contains("fecha")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                value = sdf.format(new Date((Long) value));
            }

            TextView tv = new TextView(this);
            tv.setText("• " + key + ": " + value);
            tv.setTextSize(15);
            tv.setTextColor(0xFF444444);

            containerExtra.addView(tv);
        }
    }

    /** =========================================================
     * Info del vendedor
     * ========================================================= */
    private void loadSellerInfo(String sellerId) {
        new UserRepository().getUserById(sellerId)
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    txtSellerName.setText(name != null ? name : "Vendedor");

                    sellerBlock.setOnClickListener(v -> {
                        Intent i = new Intent(this, SellerProfileActivity.class);
                        i.putExtra("sellerId", sellerId);
                        startActivity(i);
                    });
                });
    }

    /** =========================================================
     * CHAT
     * ========================================================= */
    private void startChat(Product p) {

        String buyerId = FirebaseAuth.getInstance().getUid();
        String sellerId = p.getUserId();
        String pid = p.getId();

        ChatRepository repo = new ChatRepository();

        repo.findChat(buyerId, sellerId, pid)
                .addOnSuccessListener(q -> {

                    if (!q.isEmpty()) {
                        openChat(q.getDocuments().get(0).getId(), sellerId, pid);
                        return;
                    }

                    String chatId = repo.generateId();

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", chatId);
                    data.put("user1", buyerId);
                    data.put("user2", sellerId);
                    data.put("productId", pid);
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("participants", Arrays.asList(buyerId, sellerId));

                    db.collection("chats")
                            .document(chatId)
                            .set(data)
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
     * OFERTAS — COMPRADOR ENVÍA OFERTA
     * ========================================================= */
    private void enviarOferta() {

        String buyerId = FirebaseAuth.getInstance().getUid();
        if (buyerId == null) return;

        Map<String, Object> offer = new HashMap<>();
        offer.put("buyerId", buyerId);
        offer.put("price", currentProduct.getPrice());
        offer.put("timestamp", System.currentTimeMillis());
        offer.put("status", "pending");

        db.collection("products")
                .document(currentProduct.getId())
                .collection("offers")
                .document(buyerId)
                .set(offer)
                .addOnSuccessListener(v -> {

                    Toast.makeText(this, "Oferta enviada correctamente", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al enviar oferta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** =========================================================
     * OFERTAS — VENDEDOR VE SI TIENE OFERTAS
     * ========================================================= */
    private void comprobarOfertasPendientes() {

        txtOfferAlert.setVisibility(View.GONE);

        db.collection("products")
                .document(currentProduct.getId())
                .collection("offers")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) return;

                    if (!snap.isEmpty()) {
                        txtOfferAlert.setVisibility(View.VISIBLE);

                        // Mostrar 1 alerta por compra
                        snap.getDocuments().forEach(doc -> {
                            double price = doc.getDouble("price");
                            String buyerId = doc.getString("buyerId");

                            mostrarDialogoOferta(buyerId, price);
                        });
                    }
                });
    }

    private void mostrarDialogoOferta(String buyerId, double price) {

        new AlertDialog.Builder(this)
                .setTitle("Oferta recibida")
                .setMessage("El comprador ofrece " + price + "€ por este producto")
                .setPositiveButton("Aceptar", (d, w) -> aceptarOferta(buyerId, price))
                .setNegativeButton("Rechazar", (d, w) -> rechazarOferta(buyerId))
                .show();
    }

    private void rechazarOferta(String buyerId) {

        db.collection("products")
                .document(currentProduct.getId())
                .collection("offers")
                .document(buyerId)
                .update("status", "rejected");

        Toast.makeText(this, "Oferta rechazada", Toast.LENGTH_SHORT).show();
    }

    private void aceptarOferta(String buyerId, double price) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("sold", true);
        updates.put("buyerId", buyerId);
        updates.put("soldAt", System.currentTimeMillis());

        db.collection("products")
                .document(currentProduct.getId())
                .update(updates)
                .addOnSuccessListener(v -> {

                    db.collection("products")
                            .document(currentProduct.getId())
                            .collection("offers")
                            .document(buyerId)
                            .update("status", "accepted");

                    Toast.makeText(this, "Producto vendido", Toast.LENGTH_SHORT).show();

                    currentProduct.setSold(true);
                    setupOwnerOrBuyerUI();
                });
    }

    /** =========================================================
     * VALORAR VENDEDOR — SOLO COMPRADOR
     * ========================================================= */
    private void mostrarValoracionSiSoyComprador() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || currentProduct == null) return;

        if (!currentProduct.isSold() || currentProduct.getBuyerId() == null) return;

        boolean soyComprador = uid.equals(currentProduct.getBuyerId());
        boolean vendedorEsOtro = !uid.equals(currentProduct.getUserId());

        if (!(soyComprador && vendedorEsOtro)) return;

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

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);
        android.widget.RatingBar ratingBar = view.findViewById(R.id.ratingBarVote);

        new AlertDialog.Builder(this)
                .setTitle("Valorar vendedor")
                .setView(view)
                .setPositiveButton("Enviar", (d, w) -> {

                    float stars = ratingBar.getRating();
                    if (stars <= 0) return;

                    guardarValoracion(sellerId, stars);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarValoracion(String sellerId, float stars) {

        String buyerId = FirebaseAuth.getInstance().getUid();
        if (buyerId == null) return;

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
                .addOnSuccessListener(v -> {

                    db.collection("users")
                            .document(sellerId)
                            .update(
                                    "ratingSum", FieldValue.increment((int) stars),
                                    "ratingCount", FieldValue.increment(1)
                            );

                    Toast.makeText(this, "¡Gracias por valorar!", Toast.LENGTH_SHORT).show();
                });
    }

    /** =========================================================
     * EDITAR PRODUCTO
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
                .setPositiveButton("Guardar", (d, w) ->
                        guardarEdicionProducto(
                                edtTitle.getText().toString(),
                                edtPrice.getText().toString(),
                                edtDesc.getText().toString()
                        ))
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
                    title.setText(newTitle);
                    price.setText(String.format(Locale.getDefault(), "%.2f€", priceValue));
                    description.setText(newDesc);
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                });
    }

    /** =========================================================
     * ELIMINAR
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
        db.collection("products")
                .document(currentProduct.getId())
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
