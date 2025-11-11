package com.tzolas.camisetaswallapop.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.MessageAdapter;
import com.tzolas.camisetaswallapop.models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton btnSend;

    private String chatId, sellerId, productId;
    private String currentUserId;

    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        sellerId = getIntent().getStringExtra("sellerId");
        productId = getIntent().getStringExtra("productId");

        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerView   = findViewById(R.id.recyclerMessages);
        inputMessage   = findViewById(R.id.editMessage);
        btnSend        = findViewById(R.id.btnSend);

        setupRecycler();
        listenMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    /** =========================================================
     * ✅ RecyclerView setup
     * ========================================================= */
    private void setupRecycler() {
        adapter = new MessageAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /** =========================================================
     * ✅ LEER MENSAJES EN TIEMPO REAL
     * ========================================================= */
    private void listenMessages() {

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    messageList.clear();
                    messageList.addAll(value.toObjects(Message.class));
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1); // auto-scroll
                });
    }

    /** =========================================================
     * ✅ ENVIAR MENSAJE
     * ========================================================= */
    private void sendMessage() {

        String txt = inputMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        String msgId = UUID.randomUUID().toString();

        Message m = new Message(
                msgId,
                currentUserId,
                txt,
                System.currentTimeMillis()
        );

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(msgId)
                .set(m)
                .addOnSuccessListener(aVoid -> {
                    inputMessage.setText("");
                })
                .addOnFailureListener(e ->
                        Log.e("ChatActivity", "Error enviando mensaje", e)
                );
    }

}
