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

    public Task<Void> upsertUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("photo", user.getPhoto());
        data.put("createdAt", FieldValue.serverTimestamp());

        // inicializar points y spentPoints sólo si no existen (merge)
        data.put("points", user.getPoints());      // si user.getPoints()==0 se guardará 0 en creación
        data.put("spentPoints", user.getSpentPoints());

        return db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getUserById(String uid) {
        return db.collection("users").document(uid).get();
    }

    // actualizar puntos (se usa en transacciones, pero es útil)
    public Task<Void> updatePoints(String uid, long newPoints) {
        Map<String, Object> up = new HashMap<>();
        up.put("points", newPoints);
        return db.collection("users").document(uid).update(up);
    }
}
