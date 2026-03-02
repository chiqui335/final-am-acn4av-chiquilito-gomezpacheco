package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AjustesActivity extends AppCompatActivity {

    public static final String PREFS = "config";
    public static final String KEY_MOSTRAR_CLAVES = "mostrar_claves";

    private TextView txtEmailActual;
    private SwitchMaterial switchMostrarClaves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        ImageButton btnVolver = findViewById(R.id.btnVolverAjustes);
        txtEmailActual = findViewById(R.id.txtEmailActual);
        Button btnReset = findViewById(R.id.btnResetPassword);
        switchMostrarClaves = findViewById(R.id.switchMostrarClaves);

        btnVolver.setOnClickListener(v -> finish());

        // Mostrar email actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            txtEmailActual.setText("Email: " + user.getEmail());
        } else {
            txtEmailActual.setText("Email: -");
        }

        // Preferencias
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean mostrar = prefs.getBoolean(KEY_MOSTRAR_CLAVES, false);
        switchMostrarClaves.setChecked(mostrar);

        switchMostrarClaves.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_MOSTRAR_CLAVES, isChecked).apply()
        );

        // Reset password por email
        btnReset.setOnClickListener(v -> {
            if (user == null || user.getEmail() == null) {
                Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Te envié un email para cambiar la contraseña.", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}