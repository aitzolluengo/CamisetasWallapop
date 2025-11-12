package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.User;
import com.tzolas.camisetaswallapop.repositories.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validaciones básicas
        if (email.isEmpty()) {
            editEmail.setError("El correo no puede estar vacío");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Correo inválido");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("La contraseña no puede estar vacía");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Debe tener al menos 6 caracteres");
            editPassword.requestFocus();
            return;
        }

        // Registro en Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser fu = auth.getCurrentUser();
                        if (fu != null) {

                            String uid = fu.getUid();

                            // Nombre generado del email
                            String name = email.contains("@")
                                    ? email.substring(0, email.indexOf("@"))
                                    : "Usuario";

                            // Foto nula por ahora
                            String photoUrl = null;

                            User u = new User(uid, name, email, photoUrl, null, 0 , 0);

                            // Guardar/Actualizar en Firestore
                            new UserRepository().upsertUser(u)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error guardando usuario", Toast.LENGTH_SHORT).show()
                                    );
                        }

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
