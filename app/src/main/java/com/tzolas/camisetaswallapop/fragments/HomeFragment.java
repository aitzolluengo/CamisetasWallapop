package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.AddCamisetaActivity;
import com.tzolas.camisetaswallapop.adapters.CamisetaAdapter;
import com.tzolas.camisetaswallapop.models.Camiseta;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAdd;
    private List<Camiseta> lista;
    private CamisetaAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HomeFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerCamisetas);
        btnAdd = view.findViewById(R.id.btnAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lista = new ArrayList<>();
        adapter = new CamisetaAdapter(getContext(), lista);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarCamisetas();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddCamisetaActivity.class));
        });

        return view;
    }

    private void cargarCamisetas() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Inicia sesión para ver camisetas", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("camisetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    lista.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Camiseta camiseta = doc.toObject(Camiseta.class);
                        // 🔹 Mostrar solo camisetas de otros usuarios
                        if (camiseta.getUserId() != null && !camiseta.getUserId().equals(user.getUid())) {
                            lista.add(camiseta);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar camisetas", Toast.LENGTH_SHORT).show();
                });
    }
}
