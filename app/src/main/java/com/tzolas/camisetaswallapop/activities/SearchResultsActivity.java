package com.tzolas.camisetaswallapop.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchResultsActivity extends AppCompatActivity {

    private ProductsAdapter adapter;
    private String currentCategory;
    private String currentQuery;
    private TextView txtHeader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        txtHeader = findViewById(R.id.txtHeader);
        RecyclerView recycler = findViewById(R.id.recycler);
        Button btnFilter = findViewById(R.id.btnFilter);

        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductsAdapter(this, new ArrayList<>(), product -> {
            // Abrir detalles
            startActivity(new android.content.Intent(this, ProductDetailActivity.class)
                    .putExtra("productId", product.getId()));
        });
        recycler.setAdapter(adapter);

        currentCategory = getIntent().getStringExtra("category");
        currentQuery = getIntent().getStringExtra("query");

        // Cargar productos según categoría o búsqueda
        if (currentCategory != null && !currentCategory.trim().isEmpty()) {
            txtHeader.setText("Categoría: " + currentCategory);
            loadByCategory(currentCategory);
        } else if (currentQuery != null && !currentQuery.trim().isEmpty()) {
            txtHeader.setText("Búsqueda: \"" + currentQuery + "\"");
            loadByQuery(currentQuery);
        } else {
            txtHeader.setText("Resultados");
        }

        btnFilter.setOnClickListener(v -> openFiltersDialog());
    }

    // ----------- CARGAR PRODUCTOS -----------

    private void loadByCategory(String category) {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    List<Product> filtered = new ArrayList<>();
                    String target = category.toLowerCase(Locale.getDefault()).trim();
                    for (var doc : result.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null && p.getCategory() != null) {
                            if (p.getCategory().toLowerCase(Locale.getDefault()).trim().equals(target)) {
                                filtered.add(p);
                            }
                        }
                    }
                    adapter.updateProducts(filtered);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando productos", Toast.LENGTH_SHORT).show());
    }

    private void loadByQuery(String query) {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    List<Product> filtered = new ArrayList<>();
                    String q = query.toLowerCase(Locale.getDefault()).trim();
                    for (var doc : result.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            String title = p.getTitle() != null ? p.getTitle().toLowerCase(Locale.getDefault()) : "";
                            String desc = p.getDescription() != null ? p.getDescription().toLowerCase(Locale.getDefault()) : "";
                            if (title.contains(q) || desc.contains(q)) filtered.add(p);
                        }
                    }
                    adapter.updateProducts(filtered);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error en la búsqueda", Toast.LENGTH_SHORT).show());
    }

    // ----------- FILTROS DINÁMICOS -----------

    private void openFiltersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtros");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Spinner Categoría
        String[] categories = {"Camisetas", "Cromos", "Entradas"};
        Spinner spinnerCategory = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Seleccionar categoría actual
        if (currentCategory != null) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(currentCategory)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerCategory);

        // Precio máximo
        EditText txtMaxPrice = new EditText(this);
        txtMaxPrice.setHint("Precio máximo (€)");
        txtMaxPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(txtMaxPrice);

        // Campos extra
        EditText field1 = new EditText(this);
        EditText field2 = new EditText(this);
        EditText field3 = new EditText(this);
        layout.addView(field1);
        layout.addView(field2);
        layout.addView(field3);

        // Actualizar hints y limpiar campos al cambiar categoría
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String cat = spinnerCategory.getSelectedItem().toString().toLowerCase();
                field1.setText("");
                field2.setText("");
                field3.setText("");

                switch (cat) {
                    case "camisetas":
                        field1.setHint("Equipo");
                        field2.setHint("Año");
                        field3.setHint("Talla (S,M,L,XL)");
                        field3.setVisibility(View.VISIBLE);
                        break;
                    case "cromos":
                        field1.setHint("Jugador");
                        field2.setHint("Año");
                        field3.setHint("Edición");
                        field3.setVisibility(View.VISIBLE);
                        break;
                    case "entradas":
                        field1.setHint("Estadio");
                        field2.setHint("Zona");
                        field3.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        builder.setView(layout);

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            String selectedCategory = spinnerCategory.getSelectedItem().toString().toLowerCase(Locale.getDefault());
            switch (selectedCategory) {
                case "camisetas": selectedCategory = "camiseta"; break;
                case "cromos": selectedCategory = "cromo"; break;
                case "entradas": selectedCategory = "entrada"; break;
            }

            currentCategory = selectedCategory; // actualizar categoría actual
            txtHeader.setText("Categoría: " + selectedCategory);


            double maxPrice = 0;
            try { maxPrice = Double.parseDouble(txtMaxPrice.getText().toString().trim()); } catch (Exception ignored) {}

            performFilters(selectedCategory, maxPrice,
                    field1.getText().toString().trim(),
                    field2.getText().toString().trim(),
                    field3.getText().toString().trim());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // ----------- APLICAR FILTROS -----------

    private void performFilters(String category, double maxPrice, String f1, String f2, String f3) {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    List<Product> filtered = new ArrayList<>();
                    for (var doc : result.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p == null) continue;

                        // Categoría
                        if (!p.getCategory().equalsIgnoreCase(category)) continue;

                        // Precio máximo
                        if (maxPrice > 0 && p.getPrice() > maxPrice) continue;

                        // Campos extra según categoría
                        switch (category.toLowerCase(Locale.getDefault())) {
                            case "camiseta":
                                if (!f1.isEmpty() && !p.getExtra().getOrDefault("equipo","").toString().toLowerCase().contains(f1.toLowerCase())) continue;
                                if (!f2.isEmpty() && !p.getExtra().getOrDefault("año","").toString().equals(f2)) continue;
                                if (!f3.isEmpty() && !p.getExtra().getOrDefault("talla","").toString().equalsIgnoreCase(f3)) continue;
                                break;
                            case "cromo":
                                if (!f1.isEmpty() && !p.getExtra().getOrDefault("jugador","").toString().toLowerCase().contains(f1.toLowerCase())) continue;
                                if (!f2.isEmpty() && !p.getExtra().getOrDefault("año","").toString().equals(f2)) continue;
                                if (!f3.isEmpty() && !p.getExtra().getOrDefault("edicion","").toString().toLowerCase().contains(f3.toLowerCase())) continue;
                                break;
                            case "entrada":
                                if (!f1.isEmpty() && !p.getExtra().getOrDefault("estadio","").toString().toLowerCase().contains(f1.toLowerCase())) continue;
                                if (!f2.isEmpty() && !p.getExtra().getOrDefault("zona","").toString().toLowerCase().contains(f2.toLowerCase())) continue;
                                break;
                        }

                        // Si pasa todos los filtros, añadir
                        filtered.add(p);
                    }

                    adapter.updateProducts(filtered);
                });
    }

}
