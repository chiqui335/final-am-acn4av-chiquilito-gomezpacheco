package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.cardGenerar).setOnClickListener(v ->
                startActivity(new Intent(this, GenerarClaveActivity.class))
        );

        findViewById(R.id.cardGuardadas).setOnClickListener(v ->
                startActivity(new Intent(this, VerClaves.class))
        );

        findViewById(R.id.cardAjustes).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AjustesActivity.class))
        );

        findViewById(R.id.cardLogout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, InicioActivity.class));
            finish();
        });
    }
}
