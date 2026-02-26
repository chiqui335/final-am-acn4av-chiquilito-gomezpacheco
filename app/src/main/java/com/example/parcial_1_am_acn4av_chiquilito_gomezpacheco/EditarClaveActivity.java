package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarClaveActivity extends AppCompatActivity {

    private EditText editNombre, editClave;
    private Button btnGuardar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String documentoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_clave);

        editNombre = findViewById(R.id.editNombre);
        editClave = findViewById(R.id.editClave);
        btnGuardar = findViewById(R.id.btnGuardar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        documentoId = getIntent().getStringExtra("documentId");
        String nombre = getIntent().getStringExtra("nombre");
        String clave = getIntent().getStringExtra("clave");

        // Validación para evitar crash
        if (documentoId == null) {
            Toast.makeText(this, "Error: ID no recibido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editNombre.setText(nombre);
        editClave.setText(clave);

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevaClave = editClave.getText().toString().trim();

        if (TextUtils.isEmpty(nuevoNombre) || TextUtils.isEmpty(nuevaClave)) {
            Toast.makeText(this, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String nuevaFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("nombre", nuevoNombre);
        datosActualizados.put("clave", nuevaClave);
        datosActualizados.put("fecha", nuevaFecha);

        db.collection("usuarios")
                .document(userId)
                .collection("claves")
                .document(documentoId)
                .set(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Clave actualizada", Toast.LENGTH_SHORT).show();
                    finish(); // Volver a VerClaves
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
    }
}
