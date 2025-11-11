package com.tzolas.camisetaswallapop.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ProductDetailActivity;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private HomeViewModel viewModel;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerProducts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductsAdapter(new java.util.ArrayList<>());
        recyclerView.setAdapter(adapter);

        // ✅ CLICK → abrir detalle
        adapter.setOnItemClickListener(product -> {
            Intent i = new Intent(getActivity(), ProductDetailActivity.class);
            i.putExtra("productId", product.getId());
            startActivity(i);
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.setProducts(products);
        });

        return view;
    }
}
