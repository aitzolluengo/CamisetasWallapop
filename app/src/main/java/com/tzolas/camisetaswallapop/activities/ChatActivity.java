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
import com.google.firebase.firestore.ListenerRegistration;
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

    private String chatId, sellerId, productId, currentUserId;

    private MessageAdapter adapter;
    private final List<Message> messageList = new ArrayList<>();

    private ListenerRegistration messagesListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        sellerId = getIntent().getStringExtra("sellerId");
        productId = getIntent().getStringExtra("productId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (chatId == null || currentUserId == null) {
            finish();
            return;
        }

        initViews();
        setupRecycler();
        loadChatUserInfo();
        listenMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerMessages);
        inputMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        imgChatUser = findViewById(R.id.imgChatUser);
        txtChatUserName = findViewById(R.id.txtChatUserName);
    }

    /** =========================================================
     * CONFIG LISTA
     * ========================================================= */
    private void setupRecycler() {
        adapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);

        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    /** =========================================================
     * CARGAR DATOS DEL USUARIO DEL CHAT
     * ========================================================= */
    private void loadChatUserInfo() {
        if (sellerId == null) return;

        db.collection("users").document(sellerId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    txtChatUserName.setText(doc.getString("name"));

                    String photo = doc.getString("photo");
                    Glide.with(this)
                            .load(photo != null ? photo : R.drawable.ic_user_placeholder)
                            .circleCrop()
                            .into(imgChatUser);
                });
    }

    /** =========================================================
     * 🔥 ESCUCHAR MENSAJES
     * ========================================================= */
    private void listenMessages() {

        messagesListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {

                    if (error != null || snap == null) return;

                    messageList.clear();

                    for (var doc : snap.getDocuments()) {
                        Message m = doc.toObject(Message.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            messageList.add(m);
                        }
                    }

                    markStates();

                    adapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    /** =========================================================
     * 🔥 ACTUALIZAR ESTADOS (ENTREGADO / LEÍDO)
     * ========================================================= */
    private void markStates() {

        for (Message msg : messageList) {

            // Mensaje recibido (del otro)
            if (!msg.getSenderId().equals(currentUserId)) {

                // ENTREGADO
                if (!msg.isDelivered()) {
                    db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document(msg.getId())
                            .update("delivered", true);
                }

                // LEÍDO
                if (!msg.isRead()) {
                    db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document(msg.getId())
                            .update("read", true);
                }
            }
        }
    }

    /** =========================================================
     * 🔥 ENVIAR MENSAJE
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

        m.setDelivered(false);
        m.setRead(false);

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

    /** =========================================================
     * LIMPIAR LISTENERS
     * ========================================================= */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }

    /** =========================================================
     * ACEPTAR OFERTA
     * ========================================================= */
    private void aceptarOferta(Message m) {

        String msgId = m.getId();

        // 1) Actualizar mensaje: status = accepted
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(msgId)
                .update("status", "accepted")
                .addOnSuccessListener(v -> {

                    // 2) marcar producto como vendido
                    db.collection("products")
                            .document(productId)
                            .update(
                                    "sold", true,
                                    "buyerId", m.getSenderId(),
                                    "soldAt", System.currentTimeMillis()
                            );

                    // 3) enviar mensaje del sistema
                    enviarMensajeSistema("Has aceptado la oferta. Producto vendido ✔");

                })
                .addOnFailureListener(e ->
                        Log.e("CHAT", "Error aceptando oferta", e)
                );
    }
    /** =========================================================
     * RECHAZAR OFERTA
     * ========================================================= */
    private void rechazarOferta(Message m) {

        String msgId = m.getId();

        // 1) Actualizar mensaje: status = rejected
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(msgId)
                .update("status", "rejected")
                .addOnSuccessListener(v -> {

                    // 2) enviar mensaje del sistema
                    enviarMensajeSistema("Has rechazado la oferta ❌");

                })
                .addOnFailureListener(e ->
                        Log.e("CHAT", "Error rechazando oferta", e)
                );
    }
    /** =========================================================
     * Enviar mensaje del sistema al chat
     * ========================================================= */
    private void enviarMensajeSistema(String texto) {

        String newId = UUID.randomUUID().toString();

        Message sys = new Message(
                newId,
                "system",    // identificador especial
                texto,
                System.currentTimeMillis()
        );

        sys.setType("system");

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(newId)
                .set(sys);
    }



}