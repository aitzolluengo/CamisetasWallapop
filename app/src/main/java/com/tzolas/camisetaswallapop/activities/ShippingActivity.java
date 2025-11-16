package com.tzolas.camisetaswallapop.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.repositories.OrderRepository;

public class ShippingActivity extends AppCompatActivity {

    private EditText edtAddress, edtPostal, edtPhone;
    private Button btnSave;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        edtAddress = findViewById(R.id.edtAddress);
        edtPostal = findViewById(R.id.edtPostalCode);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSaveShipping);

        productId = getIntent().getStringExtra("productId");
        if (productId == null) { finish(); return; }

        btnSave.setOnClickListener(v -> saveShipping());
    }

    private void saveShipping() {
        String addr = edtAddress.getText().toString().trim();
        String postal = edtPostal.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (addr.isEmpty() || postal.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        new OrderRepository().saveShippingInfo(productId, addr, postal, phone)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error guardando: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
