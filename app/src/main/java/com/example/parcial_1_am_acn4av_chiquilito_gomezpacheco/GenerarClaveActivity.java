package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerarClaveActivity extends AppCompatActivity {

    Button btnGuardar, btnGenerar, btnCopiar, btnCancelar, btnVerClaves, btnCerrarSesion;
    EditText etNombreClave;
    TextView txtPassword, txtLongitud;
    CheckBox checkEspeciales;
    SeekBar seekBarLongitud;

    private int longitudPassword = 12;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_clave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // IDs: tienen que existir en activity_generar_clave.xml
        btnVerClaves = findViewById(R.id.btnVerClaves);
        btnGuardar = findViewById(R.id.btnGuardar);
        etNombreClave = findViewById(R.id.etNombreClave);
        txtPassword = findViewById(R.id.txtPassword);
        btnGenerar = findViewById(R.id.btnGenerar);
        btnCopiar = findViewById(R.id.btnCopiar);
        btnCancelar = findViewById(R.id.btnCancelar);
        checkEspeciales = findViewById(R.id.checkEspeciales);
        seekBarLongitud = findViewById(R.id.seekBarLongitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Si en el layout existe btnCerrarSesion, lo conecta. Si no, lo ignora.
        try {
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
            if (btnCerrarSesion != null) {
                btnCerrarSesion.setOnClickListener(v -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, InicioActivity.class));
                    finish();
                });
            }
        } catch (Exception ignored) {}

        // SeekBar
        seekBarLongitud.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                longitudPassword = 8 + progress;
                txtLongitud.setText(longitudPassword + " caracteres");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Generar
        btnGenerar.setOnClickListener(v -> {
            String password = generarContrasenaSegura(longitudPassword);
            txtPassword.setText(password);
        });

        // Copiar
        btnCopiar.setOnClickListener(v -> {
            String password = txtPassword.getText().toString();
            if (!password.isEmpty() && !password.equals("Aquí aparecerá la contraseña")) {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("Contraseña", password);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Contraseña copiada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Primero genera una contraseña", Toast.LENGTH_SHORT).show();
            }
        });

        // Guardar
        btnGuardar.setOnClickListener(v -> {
            String password = txtPassword.getText().toString().trim();
            String nombre = etNombreClave.getText().toString().trim();

            if (password.isEmpty() || password.equals("Aquí aparecerá la contraseña")) {
                Toast.makeText(this, "Primero genera una contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nombre.isEmpty()) nombre = "Sin nombre";

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            Clave nuevaClave = new Clave(nombre, password, fecha);
            guardarClaveEnFirestore(nuevaClave);
        });

        // Cancelar / reset
        btnCancelar.setOnClickListener(v -> {
            resetearCampos();
            Toast.makeText(this, "Campos reseteados", Toast.LENGTH_SHORT).show();
        });

        // Ver claves
        btnVerClaves.setOnClickListener(v -> {
            startActivity(new Intent(this, VerClaves.class));
        });

        resetearCampos();



        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(GenerarClaveActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // opcional: cierra esta activity
        });
    }

    private void resetearCampos() {
        etNombreClave.setText("");
        txtPassword.setText("Aquí aparecerá la contraseña");
        seekBarLongitud.setProgress(longitudPassword - 8);
        txtLongitud.setText(longitudPassword + " caracteres");
        checkEspeciales.setChecked(false);
    }

    private String generarContrasenaSegura(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (checkEspeciales.isChecked()) chars += "!@#$%^&*";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private void guardarClaveEnFirestore(Clave nuevaClave) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "No hay usuario autenticado al intentar guardar clave.");
            return;
        }

        String userId = currentUser.getUid();

        db.collection("usuarios")
                .document(userId)
                .collection("claves")
                .add(nuevaClave)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(GenerarClaveActivity.this, "Guardada con éxito", Toast.LENGTH_SHORT).show();
                        resetearCampos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GenerarClaveActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error al añadir documento", e);
                    }
                });
    }
}
