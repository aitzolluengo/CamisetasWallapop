package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tzolas.camisetaswallapop.models.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * ✅ Busca si ya existe un chat entre dos usuarios para un mismo producto
     * (sin importar quién es user1 o user2).
     */
    public Task<QuerySnapshot> findChat(String user1, String user2, String productId) {
        return db.collection("chats")
                .whereEqualTo("productId", productId)
                .whereArrayContains("participants", user1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) return task;

                    // Filtrar manualmente los que también contienen user2
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            var participants = (java.util.List<String>) doc.get("participants");
                            if (participants != null && participants.contains(user2)) {
                                return db.collection("chats")
                                        .whereEqualTo("id", doc.getId())
                                        .get();
                            }
                        }
                    }
                    // Si no encontró coincidencia, devuelve lista vacía
                    return db.collection("chats")
                            .whereEqualTo("id", "none")
                            .get();
                });
    }

    /** ✅ Crear nuevo chat con lista de participantes */
    public Task<Void> createChat(Chat chat) {
        // Aseguramos que tenga campo participants
        Map<String, Object> data = new HashMap<>();
        data.put("id", chat.getId());
        data.put("user1", chat.getUser1());
        data.put("user2", chat.getUser2());
        data.put("productId", chat.getProductId());
        data.put("createdAt", chat.getCreatedAt());
        data.put("participants", Arrays.asList(chat.getUser1(), chat.getUser2()));

        return db.collection("chats")
                .document(chat.getId())
                .set(data);
    }

    /** ✅ Obtener todos los chats donde participe un usuario */
    public Task<QuerySnapshot> getChatsForUser(String uid) {
        return db.collection("chats")
                .whereArrayContains("participants", uid)
                .get();
    }

    /** ✅ Generar ID aleatorio */
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
