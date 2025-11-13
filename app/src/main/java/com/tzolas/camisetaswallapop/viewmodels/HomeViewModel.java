package com.tzolas.camisetaswallapop.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String myUid = FirebaseAuth.getInstance().getUid();

    public HomeViewModel() {
        loadAllProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    private void loadAllProducts() {

        // 1️⃣ Cargar favoritos primero si hay usuario logueado
        if (myUid != null) {
            db.collection("users")
                    .document(myUid)
                    .collection("favorites")
                    .get()
                    .addOnSuccessListener(favSnap -> {

                        Set<String> favoriteIds = new HashSet<>();
                        favSnap.forEach(doc -> favoriteIds.add(doc.getId()));

                        loadProductsWithFavorites(favoriteIds);
                    })
                    .addOnFailureListener(e -> {
                        // Si falla favoritos → cargar productos sin favoritos
                        loadProductsWithFavorites(new HashSet<>());
                    });

        } else {
            // No logueado → cargar sin favoritos
            loadProductsWithFavorites(new HashSet<>());
        }
    }

    private void loadProductsWithFavorites(Set<String> favoriteIds) {

        db.collection("products")
                .get()
                .addOnSuccessListener(query -> {

                    List<Product> list = new ArrayList<>();

                    query.getDocuments().forEach(doc -> {

                        Product p = doc.toObject(Product.class);
                        if (p == null) return;

                        // Asegurar ID
                        p.setId(doc.getId());

                        // Marcar favorito
                        p.setFavorite(favoriteIds.contains(p.getId()));

                        // Opcional: NO mostrar mis propios productos
                        if (myUid != null && p.getUserId() != null && p.getUserId().equals(myUid)) {
                            return; // saltar mis productos
                        }

                        list.add(p);
                    });

                    products.setValue(list);
                })
                .addOnFailureListener(e -> {
                    products.setValue(new ArrayList<>());
                });
    }
}
