package com.tzolas.camisetaswallapop.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.activities.MainActivity;
import com.tzolas.camisetaswallapop.R;

import java.util.HashMap;
import java.util.Map;

public class AddCamisetaActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etPrecio;
    private Button btnGuardar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camiseta);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        btnGuardar = findViewById(R.id.btnGuardar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnGuardar.setOnClickListener(v -> guardarCamiseta());
    }

    private void guardarCamiseta() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto para Firestore
        Map<String, Object> camiseta = new HashMap<>();
        camiseta.put("nombre", nombre);
        camiseta.put("descripcion", descripcion);
        camiseta.put("precio", precio);
        camiseta.put("userId", user.getUid());
        camiseta.put("timestamp", System.currentTimeMillis());

        db.collection("camisetas")
                .add(camiseta)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Camiseta añadida correctamente", Toast.LENGTH_SHORT).show();

                    // 🔁 Regresar al inicio (Home / MainActivity)
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar la camiseta", Toast.LENGTH_SHORT).show();
                });
    }
}
