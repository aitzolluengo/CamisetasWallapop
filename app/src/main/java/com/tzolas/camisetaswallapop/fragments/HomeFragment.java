package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerCamisetas);
        btnAdd = view.findViewById(R.id.btnAdd);

        lista = new ArrayList<>();
        lista.add(new Camiseta("Athletic Club", "L", 25, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Real Madrid", "M", 30, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Barcelona", "S", 28, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Betis", "L", 22, R.drawable.ic_launcher_background));

        adapter = new CamisetaAdapter(getContext(), lista);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddCamisetaActivity.class))
        );

        return view;
    }
}
