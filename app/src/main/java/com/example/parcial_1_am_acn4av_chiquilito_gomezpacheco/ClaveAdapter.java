package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ClaveAdapter extends RecyclerView.Adapter<ClaveAdapter.ViewHolder> {

    private List<Clave> listaClaves;
    private FirebaseFirestore db;
    private String userId;

    public ClaveAdapter(List<Clave> listaClaves, String userId) {
        this.listaClaves = listaClaves;
        this.userId = userId;
        db = FirebaseFirestore.getInstance();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText edtNombre, edtClave;
        Button btnGenerar, btnGuardar;

        public ViewHolder(View itemView) {
            super(itemView);
            edtNombre = itemView.findViewById(R.id.edtNombre);
            edtClave = itemView.findViewById(R.id.edtClave);
            btnGenerar = itemView.findViewById(R.id.btnGenerar);
            btnGuardar = itemView.findViewById(R.id.btnGuardar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clave, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Clave clave = listaClaves.get(position);

        holder.edtNombre.setText(clave.getNombre());
        holder.edtClave.setText(clave.getClave());

        holder.btnGenerar.setOnClickListener(v -> {
            String nueva = generarClaveAleatoria();
            holder.edtClave.setText(nueva);
        });

        holder.btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = holder.edtNombre.getText().toString();
            String nuevaClave = holder.edtClave.getText().toString();

            db.collection("usuarios")
                    .document(userId)
                    .collection("claves")
                    .document(clave.getId()) // IMPORTANTE: guardar ID en tu modelo
                    .update("nombre", nuevoNombre,
                            "clave", nuevaClave);

            Toast.makeText(holder.itemView.getContext(),
                    "Actualizado", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaClaves.size();
    }

    private String generarClaveAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(caracteres.charAt((int)(Math.random() * caracteres.length())));
        }
        return sb.toString();
    }
}
