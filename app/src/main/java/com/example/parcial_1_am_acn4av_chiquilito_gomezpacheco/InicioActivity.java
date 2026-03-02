package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class InicioActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnIngresar;
    private Button btnGoogle;           // ✅ nuevo
    private TextView textViewMessage;

    private FirebaseAuth mAuth;

    // ✅ Google Sign-In
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) return;

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account == null) return;

                    firebaseAuthWithGoogle(account.getIdToken());

                } catch (ApiException e) {
                    Log.e("GoogleLogin", "Google Sign-In falló", e);
                    Toast.makeText(this, "Google Sign-In falló", Toast.LENGTH_SHORT).show();
                    textViewMessage.setText("Google Sign-In falló: " + e.getMessage());
                    textViewMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    textViewMessage.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnGoogle = findViewById(R.id.btnGoogle);
        textViewMessage = findViewById(R.id.textViewMessage);

        // Config Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Login con Email/Pass
        btnIngresar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty()) {
                editTextEmail.setError("El email es requerido");
                editTextEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                editTextPassword.setError("La contraseña es requerida");
                editTextPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                editTextPassword.setError("La contraseña debe tener al menos 6 caracteres");
                editTextPassword.requestFocus();
                return;
            }

            signInUser(email, password);
        });

        // Login con Google
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Google...", Toast.LENGTH_SHORT).show();
            googleLauncher.launch(googleClient.getSignInIntent());
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "No se pudo obtener token de Google", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(this, "¡Bienvenido con Google!", Toast.LENGTH_LONG).show();
                    Log.d("GoogleLogin", "Firebase signInWithCredential: success " + (user != null ? user.getEmail() : ""));

                    Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("GoogleLogin", "Firebase signInWithCredential: failure", e);
                    Toast.makeText(this, "Error Google/Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void signInUser(String email, String password) {
        Toast.makeText(InicioActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        textViewMessage.setText("Iniciando sesión...");
        textViewMessage.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(InicioActivity.this, "¡Bienvenido, " + (user != null ? user.getEmail() : "") + "!",
                                    Toast.LENGTH_LONG).show();
                            textViewMessage.setText("Inicio de sesión exitoso.");
                            textViewMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w("Firebase", "signInWithEmail:failure", task.getException());

                            String errorMessage = "Error de autenticación.";
                            if (task.getException() != null) {
                                errorMessage += " " + task.getException().getMessage();
                            }
                            Toast.makeText(InicioActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            textViewMessage.setText(errorMessage);
                            textViewMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            textViewMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("Firebase", "Usuario ya logueado: " + currentUser.getEmail());
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}