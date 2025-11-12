package com.tzolas.camisetaswallapop.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    private ImageView imgChatUser;
    private TextView txtChatUserName;

    private String chatId, sellerId, productId;
    private String currentUserId;

    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ✅ Extra info del intent
        chatId = getIntent().getStringExtra("chatId");
        sellerId = getIntent().getStringExtra("sellerId");
        productId = getIntent().getStringExtra("productId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        // ✅ Referencias UI
        recyclerView   = findViewById(R.id.recyclerMessages);
        inputMessage   = findViewById(R.id.editMessage);
        btnSend        = findViewById(R.id.btnSend);
        imgChatUser    = findViewById(R.id.imgChatUser);
        txtChatUserName = findViewById(R.id.txtChatUserName);

        setupRecycler();
        listenMessages();
        loadChatUserInfo();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    /** =========================================================
     * ✅ Cargar foto + nombre del otro usuario
     * ========================================================= */
    private void loadChatUserInfo() {
        if (sellerId == null) return;

        db.collection("users")
                .document(sellerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    String photo = doc.getString("photo");

                    txtChatUserName.setText(name != null ? name : "Usuario");

                    if (photo != null && !photo.isEmpty()) {
                        Glide.with(this)
                                .load(photo)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(imgChatUser);
                    } else {
                        imgChatUser.setImageResource(R.drawable.ic_user_placeholder);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ChatActivity", "Error cargando usuario del chat", e)
                );
    }

    /** =========================================================
     * ✅ RecyclerView setup
     * ========================================================= */
    private void setupRecycler() {
        adapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // ✅ los mensajes nuevos se alinean abajo

        recyclerView.setLayoutManager(lm);
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

                    if (!messageList.isEmpty()) {
                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    }
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
                .addOnSuccessListener(aVoid -> inputMessage.setText(""))
                .addOnFailureListener(e ->
                        Log.e("ChatActivity", "Error enviando mensaje", e)
                );
    }
}
