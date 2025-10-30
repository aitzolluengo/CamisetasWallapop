package com.tzolas.camisetaswallapop.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.tzolas.camisetaswallapop.R;

public class RegisterActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            // Aquí luego haremos el registro real
            finish(); // vuelve a login
        });
    }
}
