package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tzolas.camisetaswallapop.models.Product;

public class ProductRepository {

    private final FirebaseFirestore db;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> uploadProduct(Product product) {
        return db.collection("products")
                .document(product.getId())
                .set(product);
    }

    public Task<QuerySnapshot> getProducts() {
        return db.collection("products").get();
    }
}
