package com.tzolas.camisetaswallapop.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tzolas.camisetaswallapop.R;

public class AddCamisetaActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etPrecio;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camiseta);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();

            if (nombre.isEmpty() || descripcion.isEmpty() || precio.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí luego haremos que se guarde en el servidor o BD
                Toast.makeText(this, "Camiseta añadida correctamente", Toast.LENGTH_SHORT).show();
                finish(); // volver a la pantalla anterior
            }
        });
    }
}
