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

    // ------------------------------------------------------
    // CARGAR PRODUCTOS + FAVORITOS
    // ------------------------------------------------------
    private void loadAllProducts() {

        if (myUid != null) {
            // Cargar favoritos del usuario actual
            db.collection("users")
                    .document(myUid)
                    .collection("favorites")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        Set<String> favoriteIds = new HashSet<>();
                        snapshot.forEach(doc -> favoriteIds.add(doc.getId()));

                        loadProductsWithFavorites(favoriteIds);
                    })
                    .addOnFailureListener(e -> loadProductsWithFavorites(new HashSet<>()));

        } else {
            loadProductsWithFavorites(new HashSet<>());
        }
    }

    // CARGA COMPLETA DE PRODUCTOS + MARCAR FAVORITOS
    private void loadProductsWithFavorites(Set<String> favoriteIds) {

        db.collection("products")
                .get()
                .addOnSuccessListener(query -> {

                    List<Product> list = new ArrayList<>();

                    query.forEach(doc -> {

                        Product p = doc.toObject(Product.class);
                        if (p == null) return;

                        p.setId(doc.getId());

                        if ((p.getImageUrls() == null || p.getImageUrls().isEmpty())
                                && doc.contains("imageUrl")) {

                            String oldUrl = doc.getString("imageUrl");
                            if (oldUrl != null && !oldUrl.trim().isEmpty()) {
                                List<String> convert = new ArrayList<>();
                                convert.add(oldUrl);
                                p.setImageUrls(convert);
                            }
                        }

                        if (p.getImageUrls() == null) {
                            p.setImageUrls(new ArrayList<>());
                        }

                        p.setFavorite(favoriteIds.contains(p.getId()));

                        // Opcional: Ocultar mis productos del Home
                        if (myUid != null && p.getUserId() != null && p.getUserId().equals(myUid)) {
                            return;
                        }

                        list.add(p);
                    });

                    products.setValue(list);

                })
                .addOnFailureListener(e -> products.setValue(new ArrayList<>()));
    }
}
