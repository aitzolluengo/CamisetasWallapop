package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tzolas.camisetaswallapop.models.Chat;

import java.util.Arrays;
import java.util.UUID;

public class ChatRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<QuerySnapshot> findChat(String user1, String user2, String productId) {
        return db.collection("chats")
                .whereEqualTo("productId", productId)
                .whereIn("user1", java.util.Arrays.asList(user1, user2))
                .get();
    }

    public Task<Void> createChat(Chat chat) {
        return db.collection("chats")
                .document(chat.getId())
                .set(chat);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
    public Task<QuerySnapshot> getChatsForUser(String uid) {
        return db.collection("chats")
                .whereIn("user1", Arrays.asList(uid))
                .get();
    }

}
