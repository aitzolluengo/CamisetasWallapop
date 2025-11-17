package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.SecurityRepository;

import java.util.ArrayList;
import java.util.List;

public class SellerProfileActivity extends AppCompatActivity {

    private ImageView imgSeller;
    private TextView txtSellerName, txtSellerEmail;
    private RecyclerView recycler;
    private ProductsAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private Button btnSecurity;

    private SecurityRepository securityRepository;
    private String sellerId;
    private String sellerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        imgSeller = findViewById(R.id.imgSellerProfile);
        txtSellerName = findViewById(R.id.txtSellerNameProfile);
        txtSellerEmail = findViewById(R.id.txtSellerEmailProfile);
        recycler = findViewById(R.id.recyclerSellerProducts);
        btnSecurity = findViewById(R.id.btnSecurity);

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
        securityRepository = new SecurityRepository(this);

        sellerId = getIntent().getStringExtra("sellerId");
        sellerName = getIntent().getStringExtra("sellerName");

        if (sellerId != null) {
            loadSellerInfo();
            loadSellerProducts();
            configurarBotonSeguridad();
        } else {
            Toast.makeText(this, "Error: ID de vendedor no disponible", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configurarBotonSeguridad() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && currentUserId.equals(sellerId)) {
            btnSecurity.setVisibility(View.GONE);
        } else {
            btnSecurity.setVisibility(View.VISIBLE);

            // 🔥 VERIFICAR si ya está bloqueado para cambiar el texto
            if (securityRepository.isUserBlocked(sellerId)) {
                btnSecurity.setText("Usuario bloqueado - Opciones");
            } else {
                btnSecurity.setText("Opciones de seguridad");
            }

            btnSecurity.setOnClickListener(v -> {
                mostrarOpcionesSeguridad();
            });
        }
    }

    private void mostrarOpcionesSeguridad() {
        // 🔥 CAMBIAR opciones según si está bloqueado o no
        String[] opciones;
        if (securityRepository.isUserBlocked(sellerId)) {
            opciones = new String[]{"Desbloquear usuario", "Reportar usuario"};
        } else {
            opciones = new String[]{"Bloquear usuario", "Reportar usuario"};
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Opciones de seguridad")
                .setItems(opciones, (dialog, which) -> {
                    if (securityRepository.isUserBlocked(sellerId)) {
                        // Si está bloqueado
                        switch (which) {
                            case 0:
                                mostrarDialogoDesbloquear();
                                break;
                            case 1:
                                mostrarDialogoReportar();
                                break;
                        }
                    } else {
                        // Si NO está bloqueado
                        switch (which) {
                            case 0:
                                mostrarDialogoBloquear();
                                break;
                            case 1:
                                mostrarDialogoReportar();
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoBloquear() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Bloquear usuario")
                .setMessage("¿Estás seguro de que quieres bloquear a " + sellerName + "?\n\nNo podrás:\n• Ver sus productos\n• Recibir sus mensajes\n• Ver sus publicaciones en búsquedas")
                .setPositiveButton("Bloquear", (dialog, which) -> {
                    boolean exito = securityRepository.blockUser(sellerId, sellerName);
                    if (exito) {
                        Toast.makeText(this, "Usuario bloqueado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al bloquear usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoReportar() {
        String[] tiposReporte = {
                "Contenido inapropiado",
                "Spam",
                "Información falsa",
                "Comportamiento sospechoso",
                "Productos prohibidos",
                "Otro"
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar tipo de reporte")
                .setItems(tiposReporte, (dialog, which) -> {
                    String tipo = tiposReporte[which];
                    mostrarDialogoDetallesReporte(tipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoDetallesReporte(String tipo) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_details, null);
        EditText edtDescripcion = dialogView.findViewById(R.id.edtReportDescription);

        new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Reportar - " + tipo)
                .setPositiveButton("Enviar reporte", (dialog, which) -> {
                    String descripcion = edtDescripcion.getText().toString().trim();
                    if (descripcion.isEmpty()) {
                        Toast.makeText(this, "Por favor describe el problema", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    securityRepository.reportUser(sellerId, sellerName, tipo, descripcion);
                    Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
                        if (p != null) productList.add(p);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
    private void mostrarDialogoDesbloquear() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Desbloquear usuario")
                .setMessage("¿Estás seguro de que quieres desbloquear a " + sellerName + "?\n\nPodrás:\n• Ver sus productos\n• Recibir sus mensajes\n• Ver sus publicaciones en búsquedas")
                .setPositiveButton("Desbloquear", (dialog, which) -> {
                    boolean exito = securityRepository.unblockUser(sellerId);
                    if (exito) {
                        Toast.makeText(this, "Usuario desbloqueado correctamente", Toast.LENGTH_SHORT).show();
                        // Actualizar botón
                        btnSecurity.setText("Opciones de seguridad");
                        // Recargar productos (por si estaban ocultos)
                        loadSellerProducts();
                    } else {
                        Toast.makeText(this, "Error al desbloquear usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
