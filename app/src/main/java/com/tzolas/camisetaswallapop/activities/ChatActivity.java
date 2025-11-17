package com.tzolas.camisetaswallapop.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.MessageAdapter;
import com.tzolas.camisetaswallapop.models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    // 🟣 Info del producto para oferta
    private int productPricePoints = 0;   // precio en puntos
    private String productOwnerId;        // dueño del producto (por seguridad)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        sellerId = getIntent().getStringExtra("sellerId");     // dueño del producto
        productId = getIntent().getStringExtra("productId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (chatId == null || currentUserId == null || productId == null) {
            finish();
            return;
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                markAllAsReadBeforeExit();   // 🔥 1. Marca como leído al instante
                setResult(RESULT_OK);        // 🔥 2. Informa al fragment
                finish();                    // 🔥 3. Cierra chat
            }
        });



        initViews();
        setupRecycler();
        loadChatUserInfo();
        loadProductInfoForChat(); // 🟣 cargamos precio + owner
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
        adapter = new MessageAdapter(
                messageList,
                currentUserId,
                // 🟣 Listener para aceptar / rechazar oferta desde el mensaje
                new MessageAdapter.OnOfferActionListener() {
                    @Override
                    public void onAccept(Message m) {
                        aceptarOferta(m);
                    }

                    @Override
                    public void onReject(Message m) {
                        rechazarOferta(m);
                    }
                }
        );

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);

        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }
    private void markAllAsReadBeforeExit() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot d : snap) {
                        String sender = d.getString("senderId");
                        if (sender != null && !sender.equals(currentUserId)) {
                            d.getReference().update("read", true);
                        }
                    }
                });
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
     * CARGAR INFO DEL PRODUCTO: PRECIO EN PUNTOS
     * ========================================================= */
    private void loadProductInfoForChat() {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    productOwnerId = doc.getString("userId");

                    Double price = doc.getDouble("price");
                    if (price != null) {
                        // asumiendo que 1€ = 1 punto, o ya son puntos directamente
                        productPricePoints = (int) Math.round(price);
                    }
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

                    // 🔥 ACTUALIZAR EN TIEMPO REAL
                    sendBroadcast(new Intent("CHAT_UPDATED"));
                });
    }

    /** =========================================================
     * 🔥 ACTUALIZAR ESTADOS (ENTREGADO / LEÍDO)
     * ========================================================= */
    private void markStates() {

        for (Message msg : messageList) {

            if (!msg.getSenderId().equals(currentUserId)) {

                if (!msg.isDelivered()) {
                    db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document(msg.getId())
                            .update("delivered", true);
                }

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
     * 🔥 ENVIAR MENSAJE NORMAL
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
        m.setType("text");

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
     * 🔥 ENVIAR OFERTA (PRECIO DEL PRODUCTO)
     * ========================================================= */

    /** =========================================================
     * ACEPTAR OFERTA: mover puntos + marcar producto vendido
     * ========================================================= */
    private void aceptarOferta(Message m) {

        if (m == null || !"offer".equals(m.getType())) return;
        if (!"pending".equals(m.getStatus())) return;

        String buyerId = m.getSenderId();
        String sellerUid = productOwnerId;  // debería ser el dueño del producto

        if (sellerUid == null || buyerId == null) return;

        int offerPoints = m.getOfferPrice();

        // transacción para mover puntos y marcar producto vendido
        db.runTransaction(trx -> {

            DocumentReference buyerRef = db.collection("users").document(buyerId);
            DocumentReference sellerRef = db.collection("users").document(sellerUid);
            DocumentReference productRef = db.collection("products").document(productId);
            DocumentReference msgRef = db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(m.getId());

            DocumentSnapshot buyerSnap = trx.get(buyerRef);
            DocumentSnapshot sellerSnap = trx.get(sellerRef);
            DocumentSnapshot prodSnap = trx.get(productRef);

            Long buyerPoints = buyerSnap.getLong("points");
            if (buyerPoints == null) buyerPoints = 0L;

            Boolean alreadySold = prodSnap.getBoolean("sold");
            if (alreadySold != null && alreadySold) {
                throw new FirebaseFirestoreException(
                        "Producto ya vendido",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION
                );
            }

            if (buyerPoints < offerPoints) {
                throw new FirebaseFirestoreException(
                        "El comprador no tiene puntos suficientes",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION
                );
            }

            Long sellerPoints = sellerSnap.getLong("points");
            if (sellerPoints == null) sellerPoints = 0L;

            // Actualizar puntos
            trx.update(buyerRef,
                    "points", buyerPoints - offerPoints,
                    "spentPoints", FieldValue.increment(offerPoints));

            trx.update(sellerRef,
                    "points", sellerPoints + offerPoints);

            // Marcar producto vendido
            trx.update(productRef,
                    "sold", true,
                    "buyerId", buyerId,
                    "soldAt", System.currentTimeMillis());

            // Marcar mensaje como aceptado
            trx.update(msgRef, "status", "accepted");

            return null;
        }).addOnSuccessListener(v -> {
            Log.d("CHAT", "Oferta aceptada, producto vendido y puntos movidos");
            enviarMensajeSistema("✅ Has aceptado la oferta. Producto vendido.");
        }).addOnFailureListener(e -> {
            Log.e("CHAT", "Error al aceptar oferta", e);
            enviarMensajeSistema("❌ No se pudo aceptar la oferta: " + e.getMessage());
        });
    }

    /** =========================================================
     * RECHAZAR OFERTA
     * ========================================================= */
    private void rechazarOferta(Message m) {

        if (m == null || !"offer".equals(m.getType())) return;

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(m.getId())
                .update("status", "rejected")
                .addOnSuccessListener(v -> {
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
                "system",
                texto,
                System.currentTimeMillis()
        );

        sys.setType("system");
        sys.setDelivered(true);
        sys.setRead(true);

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(newId)
                .set(sys);
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
    @Override
    protected void onResume() {
        super.onResume();
        marcarMensajesComoLeidos();
    }

    private void marcarMensajesComoLeidos() {
        if (chatId == null || currentUserId == null) return;

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(q -> {
                    for (DocumentSnapshot d : q.getDocuments()) {
                        String sender = d.getString("senderId");

                        // solo marco como leído si NO soy yo el que envió
                        if (sender != null && !sender.equals(currentUserId)) {
                            d.getReference().update("read", true);
                        }
                    }

                    // notif a fragment para que quite el (1)
                });
    }

    // Cuando salimos del chat → refrescar lista en fragment
    @Override
    protected void onPause() {
        super.onPause();
        sendBroadcast(new Intent("CHAT_UPDATED"));
    }



}
