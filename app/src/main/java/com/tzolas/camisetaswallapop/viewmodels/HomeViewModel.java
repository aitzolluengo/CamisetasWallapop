package com.tzolas.camisetaswallapop.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.ProductRepository;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private ProductRepository repo = new ProductRepository();

    public HomeViewModel() {
        loadProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    private void loadProducts() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUserId = (user != null) ? user.getUid() : "";

        repo.getProducts().addOnSuccessListener(query -> {

            List<Product> list = new ArrayList<>();

            for (DocumentSnapshot d : query.getDocuments()) {
                Product p = d.toObject(Product.class);

                if (p.getUserId() != null && !p.getUserId().equals(myUserId)) {
                    list.add(p);
                }
            }

            products.setValue(list);
        });
    }
}
