package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.SearchResultsActivity;

public class BuscarFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buscar_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.searchView);
        MaterialCardView cardCromos = view.findViewById(R.id.cardCromos);
        MaterialCardView cardCamisetas = view.findViewById(R.id.cardCamisetas);
        MaterialCardView cardEntradas = view.findViewById(R.id.cardEntradas);

        // Buscar por texto
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                openResults(null, query);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) { return false; }
        });

        // Categorías
        cardCromos.setOnClickListener(v -> openResults("cromo", null));
        cardCamisetas.setOnClickListener(v -> openResults("camiseta", null));
        cardEntradas.setOnClickListener(v -> openResults("entrada", null));
    }

    private void openResults(String category, String query) {
        Intent i = new Intent(requireContext(), SearchResultsActivity.class);
        if (category != null) i.putExtra("category", category);
        if (query != null && !query.trim().isEmpty()) i.putExtra("query", query.trim());
        startActivity(i);
    }
}
