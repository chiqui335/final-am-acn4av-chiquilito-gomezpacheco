package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

    // UI común
    Button btnGuardar, btnGenerar, btnCopiar;
    EditText etNombreClave;
    TextView txtPassword;

    ImageButton btnBack;

    // Modo
    RadioGroup rgModo;
    RadioButton rbPassword, rbPassphrase;

    LinearLayout panelPassword, panelPassphrase;

    // Panel password
    TextView txtLongitud;
    CheckBox checkEspeciales;
    SeekBar seekBarLongitud;
    private int longitudPassword = 12;

    // Panel passphrase
    SeekBar seekBarPalabras;
    TextView txtPalabras;
    CheckBox checkGuiones, checkCapitalizar;
    private int cantPalabras = 4;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Lista simple de palabras (sin internet)
    private static final String[] WORDS = {
            "luna","nube","rio","bosque","sol","brisa","faro","norte","cielo","puma",
            "mate","casa","llave","codigo","pixel","web","azul","verde","fuego","hielo",
            "dato","red","puerto","nexo","viento","ruta","campo","playa","montana","valle"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_clave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Comunes
        btnGuardar = findViewById(R.id.btnGuardar);
        btnGenerar = findViewById(R.id.btnGenerar);
        btnCopiar = findViewById(R.id.btnCopiar);
        etNombreClave = findViewById(R.id.etNombreClave);
        txtPassword = findViewById(R.id.txtPassword);
        btnBack = findViewById(R.id.btnBack);

        // Modo + paneles
        rgModo = findViewById(R.id.rgModo);
        rbPassword = findViewById(R.id.rbPassword);
        rbPassphrase = findViewById(R.id.rbPassphrase);
        panelPassword = findViewById(R.id.panelPassword);
        panelPassphrase = findViewById(R.id.panelPassphrase);

        // Password panel
        seekBarLongitud = findViewById(R.id.seekBarLongitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        checkEspeciales = findViewById(R.id.checkEspeciales);

        // Passphrase panel
        seekBarPalabras = findViewById(R.id.seekBarPalabras);
        txtPalabras = findViewById(R.id.txtPalabras);
        checkGuiones = findViewById(R.id.checkGuiones);
        checkCapitalizar = findViewById(R.id.checkCapitalizar);

        // Inicializar sliders
        initSeekBars();

        // Cambiar modo
        rgModo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPassword) {
                panelPassword.setVisibility(View.VISIBLE);
                panelPassphrase.setVisibility(View.GONE);
            } else {
                panelPassword.setVisibility(View.GONE);
                panelPassphrase.setVisibility(View.VISIBLE);
            }
            txtPassword.setText("Aquí aparecerá la contraseña");
        });

        // Generar
        btnGenerar.setOnClickListener(v -> {
            String result;
            if (rbPassword.isChecked()) {
                result = generarContrasenaSegura(longitudPassword);
            } else {
                result = generarPassphrase(cantPalabras, checkGuiones.isChecked(), checkCapitalizar.isChecked());
            }
            txtPassword.setText(result);
        });

        // Copiar
        btnCopiar.setOnClickListener(v -> {
            String value = txtPassword.getText().toString();
            if (value.isEmpty() || value.equals("Aquí aparecerá la contraseña")) {
                Toast.makeText(this, "Primero generá una clave", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Clave", value));
            Toast.makeText(this, "Copiada", Toast.LENGTH_SHORT).show();
        });

        // Guardar
        btnGuardar.setOnClickListener(v -> {
            String value = txtPassword.getText().toString().trim();
            String nombre = etNombreClave.getText().toString().trim();

            if (value.isEmpty() || value.equals("Aquí aparecerá la contraseña")) {
                Toast.makeText(this, "Primero generá una clave", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nombre.isEmpty()) nombre = "Sin nombre";

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // Guardamos el tipo para tu informe / debug
            String tipo = rbPassword.isChecked() ? "password" : "passphrase";

            Clave nuevaClave = new Clave(nombre, value, fecha);
            nuevaClave.setTipo(tipo); // <- agregalo al modelo (te digo abajo)

            guardarClaveEnFirestore(nuevaClave);
        });

        // Back
        btnBack.setOnClickListener(v -> {
            finish();
        });

        resetearCampos();
    }

    private void initSeekBars() {
        // Longitud password: tu lógica actual (8 + progress)
        seekBarLongitud.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                longitudPassword = 8 + progress; // 8..20 si max=12
                txtLongitud.setText(longitudPassword + " caracteres");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Palabras passphrase: 3 + progress (si max=6 -> 3..9)
        seekBarPalabras.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cantPalabras = 3 + progress;
                txtPalabras.setText(cantPalabras + " palabras");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void resetearCampos() {
        etNombreClave.setText("");
        txtPassword.setText("Aquí aparecerá la contraseña");

        // Resets razonables
        checkEspeciales.setChecked(false);
        checkGuiones.setChecked(false);
        checkCapitalizar.setChecked(false);

        // Dejá modo password por defecto
        rbPassword.setChecked(true);
        panelPassword.setVisibility(View.VISIBLE);
        panelPassphrase.setVisibility(View.GONE);
    }

    private String generarContrasenaSegura(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (checkEspeciales.isChecked()) chars += "!@#$%^&*";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generarPassphrase(int palabras, boolean guiones, boolean capitalizar) {
        SecureRandom random = new SecureRandom();
        String sep = guiones ? "-" : " ";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palabras; i++) {
            String w = WORDS[random.nextInt(WORDS.length)];
            if (capitalizar) {
                w = w.substring(0,1).toUpperCase() + w.substring(1);
            }
            if (i > 0) sb.append(sep);
            sb.append(w);
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
                    @Override public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(GenerarClaveActivity.this, "Guardada con éxito", Toast.LENGTH_SHORT).show();
                        resetearCampos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GenerarClaveActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error al añadir documento", e);
                    }
                });
    }
}