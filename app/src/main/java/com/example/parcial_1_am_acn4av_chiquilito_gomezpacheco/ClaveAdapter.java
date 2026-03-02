package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaveAdapter extends RecyclerView.Adapter<ClaveAdapter.VH> {

    private final List<Clave> listaClaves;
    private final String userId;
    private final FirebaseFirestore db;

    // mostrar claves por defecto
    private final boolean mostrarPorDefecto;

    // guarda los IDs de las claves que están visibles manualmente
    private final Set<String> visibles = new HashSet<>();

    // constructor (con mostrarPorDefecto)
    public ClaveAdapter(List<Clave> listaClaves, String userId, boolean mostrarPorDefecto) {
        this.listaClaves = listaClaves;
        this.userId = userId;
        this.db = FirebaseFirestore.getInstance();
        this.mostrarPorDefecto = mostrarPorDefecto;
    }

    public ClaveAdapter(List<Clave> listaClaves, String userId) {
        this(listaClaves, userId, false);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clave, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Clave item = listaClaves.get(position);

        String titulo = item.getNombre();
        String fecha = item.getFecha();
        String claveReal = item.getClave();

        if (titulo == null || titulo.trim().isEmpty()) titulo = "(Sin nombre)";
        if (fecha == null) fecha = "";
        if (claveReal == null) claveReal = "";

        h.txtTitulo.setText(titulo);
        h.txtUsuario.setText(fecha);

        String id = item.getId();

        // Visible si:
        // - el usuario la “abrió” tocando el ojito
        // - O está activado “mostrar por defecto”
        boolean estaVisible = mostrarPorDefecto || (id != null && visibles.contains(id));

        h.txtPassword.setText(estaVisible ? claveReal : "••••••••••");

        // Ver/Ocultar
        h.btnVer.setOnClickListener(v -> {
            if (id == null) return;

            if (visibles.contains(id)) visibles.remove(id);
            else visibles.add(id);

            notifyItemChanged(position);
        });

        // Copiar
        String finalClaveReal = claveReal;
        h.btnCopiar.setOnClickListener(v -> {
            if (finalClaveReal.isEmpty()) {
                Toast.makeText(v.getContext(), "No hay clave para copiar", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("clave", finalClaveReal));
            Toast.makeText(v.getContext(), "Clave copiada", Toast.LENGTH_SHORT).show();
        });

        // Eliminar
        h.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar")
                    .setMessage("¿Querés eliminar esta clave guardada?")
                    .setPositiveButton("Sí", (d, which) -> borrar(item, v.getContext()))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void borrar(Clave item, Context context) {
        String docId = item.getId();
        if (docId == null || docId.isEmpty()) {
            Toast.makeText(context, "No se pudo identificar el registro.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios")
                .document(userId)
                .collection("claves")
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    int pos = -1;
                    for (int i = 0; i < listaClaves.size(); i++) {
                        if (docId.equals(listaClaves.get(i).getId())) { pos = i; break; }
                    }
                    if (pos != -1) {
                        listaClaves.remove(pos);
                        notifyItemRemoved(pos);
                    } else {
                        notifyDataSetChanged();
                    }

                    visibles.remove(docId);
                    Toast.makeText(context, "Clave eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public int getItemCount() {
        return listaClaves.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtUsuario, txtPassword;
        ImageButton btnVer, btnCopiar, btnEliminar;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloItem);
            txtUsuario = itemView.findViewById(R.id.txtUsuarioItem);
            txtPassword = itemView.findViewById(R.id.txtPasswordItem);

            btnVer = itemView.findViewById(R.id.btnVer);
            btnCopiar = itemView.findViewById(R.id.btnCopiar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}