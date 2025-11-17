package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tzolas.camisetaswallapop.repositories.SecurityRepository;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ProductDetailActivity;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private HomeViewModel viewModel;

    private TextView btnAll, btnFavs;

    private List<Product> fullList = new ArrayList<>();
    private List<Product> favList = new ArrayList<>();

    private boolean showingFavs = false;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // UI
        recyclerView = view.findViewById(R.id.recyclerProducts);
        btnAll = view.findViewById(R.id.btnFilterAll);
        btnFavs = view.findViewById(R.id.btnFilterFavs);

        // Recycler
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductsAdapter(requireContext(), new ArrayList<>(), product -> {
            Intent i = new Intent(getActivity(), ProductDetailActivity.class);
            i.putExtra("productId", product.getId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        // ⭐ IMPORTANTE: callback para reaccionar en tiempo real cuando cambia un favorito
        adapter.setOnFavoriteChangeListener(product -> {

            // actualizar fullList
            for (Product p : fullList) {
                if (p.getId().equals(product.getId())) {
                    p.setFavorite(product.isFavorite());
                    break;
                }
            }

            // regenerar favList
            favList = new ArrayList<>();
            for (Product p : fullList) {
                if (p.isFavorite()) favList.add(p);
            }

            // si está mostrando favoritos → refrescar inmediatamente
            if (showingFavs) {
                adapter.updateProducts(favList);
            }
        });

        // ViewModel
        // ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {

            fullList = products != null ? products : new ArrayList<>();

            long now = System.currentTimeMillis();
            long oneMinute = 60 * 1000;

            // filtrar productos vendidos hace más de 1 minuto
            List<Product> filtered = new ArrayList<>();
            for (Product p : fullList) {
                if (!p.isSold()) {
                    filtered.add(p);
                } else {
                    Long soldAt = p.getSoldAt();
                    if (soldAt != null && soldAt + oneMinute > now) {
                        filtered.add(p); // se vendió hace menos de 1 minuto → mostrar temporalmente
                    }
                }
            }

            fullList = filtered;

            // 🔥🔥🔥 NUEVO: FILTRAR productos de usuarios bloqueados
            SecurityRepository securityRepo = new SecurityRepository(requireContext());
            List<Product> sinUsuariosBloqueados = securityRepo.filterBlockedProducts(fullList);
            fullList = sinUsuariosBloqueados;

            // generar lista favoritos
            favList = new ArrayList<>();
            for (Product p : fullList) {
                if (p.isFavorite()) favList.add(p);
            }

            // mostrar todos por defecto
            showingFavs = false;
            showAll();
        });

        return view;
    }

    /** Mostrar todos los productos */
    private void showAll() {
        adapter.updateProducts(fullList);

        btnAll.setBackgroundResource(R.drawable.bg_filter_selected);
        btnFavs.setBackgroundResource(R.drawable.bg_filter_unselected);

        btnAll.setTextColor(0xFFFFFFFF);
        btnFavs.setTextColor(0xFF444444);
    }

    /** Mostrar solo favoritos */
    private void showFavs() {
        adapter.updateProducts(favList);

        btnFavs.setBackgroundResource(R.drawable.bg_filter_selected);
        btnAll.setBackgroundResource(R.drawable.bg_filter_unselected);

        btnFavs.setTextColor(0xFFFFFFFF);
        btnAll.setTextColor(0xFF444444);
    }
}
