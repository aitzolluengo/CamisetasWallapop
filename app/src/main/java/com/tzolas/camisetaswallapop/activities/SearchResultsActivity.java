package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ProductsAdapter;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Muestra resultados por categoría o por texto.
 * Si llega "category", carga productos aleatorios de esa categoría.
 * Si llega "query", filtra por texto en el título/descripción.
 *
 * Sustituye los métodos loadFromDataSource(...) por tu repo real (Firestore, etc).
 */
public class SearchResultsActivity extends AppCompatActivity {

    private ProductsAdapter adapter;
    private final List<Product> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        TextView header = findViewById(R.id.txtHeader);
        RecyclerView recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductsAdapter(this, new ArrayList<>(), product -> {
            // Abre detalle
            Intent i = new Intent(this, com.tzolas.camisetaswallapop.activities.ProductDetailActivity.class);
            i.putExtra("productId", product.getId());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        String category = getIntent().getStringExtra("category");
        String query = getIntent().getStringExtra("query");

        if (category != null) {
            header.setText("Aleatorios: " + category);
            loadRandomByCategory(category);
        } else if (query != null) {
            header.setText("Búsqueda: \"" + query + "\"");
            loadByQuery(query);
        } else {
            header.setText("Resultados");
            adapter.setProducts(new ArrayList<>());
        }
    }

    // ====== Simulación / hooks hacia tu repositorio real ======
    // Reemplaza estos métodos por llamadas a Firestore / API y luego llama a "onProductsLoaded(...)"

    private void loadRandomByCategory(String category) {
        List<Product> all = loadFromDataSource(); // TODO: repositorio real
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) {
            if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        // Mezclar para aleatorio
        Collections.shuffle(filtered);
        onProductsLoaded(filtered);
    }

    private void loadByQuery(String query) {
        String q = query.toLowerCase();
        List<Product> all = loadFromDataSource(); // TODO: repositorio real
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) {
            String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
            String desc  = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
            if (title.contains(q) || desc.contains(q)) {
                filtered.add(p);
            }
        }
        onProductsLoaded(filtered);
    }

    private void onProductsLoaded(List<Product> products) {
        // Si quieres "muchos aleatorios", puedes limitar a, por ejemplo, 50:
        // if (products.size() > 50) products = products.subList(0, 50);
        adapter.setProducts(products);
    }

    // --- Sustituye esto por tu fuente real (Firestore, etc) ---
    private List<Product> loadFromDataSource() {
        // TODO: sustituir por ChatRepository/UserRepository/ProductRepository si ya lo tienes.
        // Aquí devuelvo vacío para compilar; tu implementación debe traer productos reales.
        return data;
    }
}
