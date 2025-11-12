package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.User;
import com.tzolas.camisetaswallapop.repositories.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private com.google.android.gms.common.SignInButton btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Ir a registro
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // Login normal (email/contraseña)
        btnLogin.setOnClickListener(v -> loginUser());

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Este ID sale de google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            upsertUserToFirestore(auth.getCurrentUser());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Introduce tu correo");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Introduce tu contraseña");
            editPassword.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        upsertUserToFirestore(user);

                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, t -> {
                    if (t.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        upsertUserToFirestore(user);

                        Toast.makeText(this, "Bienvenido " + (user != null ? user.getDisplayName() : ""), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error en la autenticación con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ✅ Guarda / actualiza el usuario en Firestore
     */
    private void upsertUserToFirestore(FirebaseUser fu) {
        if (fu == null) return;

        String uid = fu.getUid();
        String email = fu.getEmail();

        // nombre
        String displayName = fu.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            if (email != null && email.contains("@")) {
                displayName = email.substring(0, email.indexOf("@"));
            } else {
                displayName = "Usuario";
            }
        }

        // foto
        String photoUrl = (fu.getPhotoUrl() != null) ? fu.getPhotoUrl().toString() : null;

        // crear User
        User u = new User(uid, displayName, email, photoUrl, null, 0, 0);

        // guardar en Firestore
        new UserRepository().upsertUser(u);
    }
}
