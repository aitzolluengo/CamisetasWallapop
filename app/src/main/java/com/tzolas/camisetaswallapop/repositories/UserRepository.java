package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tzolas.camisetaswallapop.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Crea o actualiza (merge) los datos básicos del usuario
    public Task<Void> upsertUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("photo", user.getPhoto());
        // solo setear createdAt si no existe
        data.put("createdAt", FieldValue.serverTimestamp());

        return db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getUserById(String uid) {
        return db.collection("users").document(uid).get();
    }
}
