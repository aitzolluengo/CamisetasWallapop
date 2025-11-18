package com.tzolas.camisetaswallapop.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.models.Product;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecurityRepository {

    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    private static final String PREFS_NAME = "security_prefs";
    private static final String KEY_BLOCKED_USERS = "blocked_users";

    public SecurityRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
    }

    // Bloquear usuario
    public boolean blockUser(String userId, String userName) {
        try {
            Set<String> blockedUsers = getBlockedUsers();
            blockedUsers.add(userId);
            sharedPreferences.edit().putStringSet(KEY_BLOCKED_USERS, blockedUsers).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //  Verificar si usuario está bloqueado
    public boolean isUserBlocked(String userId) {
        Set<String> blockedUsers = getBlockedUsers();
        return blockedUsers.contains(userId);
    }

    // Obtener lista de usuarios bloqueados
    public Set<String> getBlockedUsers() {
        return sharedPreferences.getStringSet(KEY_BLOCKED_USERS, new HashSet<>());
    }

    // Reportar usuario
    public void reportUser(String userId, String userName, String reportType, String description) {
        try {
            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            if (currentUserId == null) return;

            Map<String, Object> report = new HashMap<>();
            report.put("reportedUserId", userId);
            report.put("reportedUserName", userName);
            report.put("reporterId", currentUserId);
            report.put("reportType", reportType);
            report.put("description", description);
            report.put("timestamp", System.currentTimeMillis());
            report.put("status", "pending");

            db.collection("reports")
                    .document()
                    .set(report);

        } catch (Exception e) {
            // Error silencioso
        }
    }

    // FILTRAR productos de usuarios no bloqueados
    public List<Product> filterBlockedProducts(List<Product> products) {
        List<Product> filtered = new java.util.ArrayList<>();
        for (Product product : products) {
            if (!isUserBlocked(product.getUserId())) {
                filtered.add(product);
            }
        }
        return filtered;
    }
    public boolean unblockUser(String userId) {
        try {
            Set<String> blockedUsers = getBlockedUsers();
            blockedUsers.remove(userId);
            sharedPreferences.edit().putStringSet(KEY_BLOCKED_USERS, blockedUsers).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}