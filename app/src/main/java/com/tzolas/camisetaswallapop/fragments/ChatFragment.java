package com.tzolas.camisetaswallapop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.MainActivity;
import com.tzolas.camisetaswallapop.adapters.ChatListAdapter;
import com.tzolas.camisetaswallapop.models.Chat;
import com.tzolas.camisetaswallapop.models.ChatPreview;

import java.util.*;

public class ChatFragment extends Fragment {

    private RecyclerView recycler;
    private ChatListAdapter adapter;
    private final List<ChatPreview> chatList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration chatListener;

    private String myUid;

    // MAP para listeners de cada chat individual
    private final Map<String, ListenerRegistration> messageListeners = new HashMap<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        recycler = v.findViewById(R.id.recyclerChats);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatListAdapter(chatList);
        recycler.setAdapter(adapter);

        myUid = FirebaseAuth.getInstance().getUid();

        listenChatsRealtime();

        return v;
    }

    // =====================================================================
    // 🔥 1) Listener en TIEMPO REAL para todos los chats
    // =====================================================================
    private void listenChatsRealtime() {

        if (myUid == null) return;

        // Elimina listeners previos
        if (chatListener != null) chatListener.remove();
        for (ListenerRegistration l : messageListeners.values()) l.remove();
        messageListeners.clear();

        chatListener = db.collection("chats")
                .whereArrayContains("participants", myUid)
                .addSnapshotListener((query, error) -> {

                    if (error != null || query == null) return;

                    chatList.clear(); // reseteamos chats

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) loadChatPreview(chat);
                    }
                });
    }

    // =====================================================================
    // 🔥 2) Cargar info de PREVIEW para cada chat
    // =====================================================================
    private void loadChatPreview(Chat chat) {

        String chatId = chat.getId();
        String otherUid = chat.getUser1().equals(myUid) ? chat.getUser2() : chat.getUser1();

        db.collection("users").document(otherUid).get()
                .addOnSuccessListener(userDoc -> {

                    String name = userDoc.getString("name");
                    String photo = userDoc.getString("photo");

                    ChatPreview preview = new ChatPreview(
                            chatId,
                            otherUid,
                            name != null ? name : "Usuario",
                            photo,
                            "",
                            0,
                            chat.getProductId()
                    );

                    chatList.add(preview);
                    listenLastMessageRealtime(preview);
                    listenUnreadRealtime(preview);
                });
    }

    // =====================================================================
    // 🔥 3) Escuchar el ÚLTIMO mensaje en tiempo real
    // =====================================================================
    private void listenLastMessageRealtime(ChatPreview preview) {

        db.collection("chats")
                .document(preview.getChatId())
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((query, error) -> {

                    if (query != null && !query.isEmpty()) {

                        DocumentSnapshot m = query.getDocuments().get(0);

                        // Texto
                        preview.setLastMessage(
                                m.getString("text") != null ? m.getString("text") : "Mensaje"
                        );

                        // Fecha
                        Long ts = m.getLong("timestamp");
                        preview.setLastMessageTime(ts != null ? ts : 0L);

                        refreshList();
                    }
                });
    }

    // =====================================================================
    // 🔥 4) Escuchar número de mensajes sin leer en tiempo real
    // =====================================================================
    private void listenUnreadRealtime(ChatPreview preview) {

        // Si ya existe listener, eliminamos antes
        if (messageListeners.containsKey(preview.getChatId())) {
            messageListeners.get(preview.getChatId()).remove();
        }

        ListenerRegistration listener = db.collection("chats")
                .document(preview.getChatId())
                .collection("messages")
                .whereEqualTo("read", false)
                .addSnapshotListener((query, error) -> {

                    int unread = 0;

                    if (query != null) {
                        for (DocumentSnapshot d : query.getDocuments()) {
                            String sender = d.getString("senderId");
                            if (sender != null && !sender.equals(myUid)) unread++;
                        }
                    }

                    preview.setUnreadCount(unread);
                    refreshList();
                });

        messageListeners.put(preview.getChatId(), listener);
    }

    // =====================================================================
    // 🔥 Refrescar UI + badge
    // =====================================================================
    private void refreshList() {

        // ordenar por último mensaje
        chatList.sort(Comparator.comparingLong(ChatPreview::getLastMessageTime).reversed());

        adapter.notifyDataSetChanged();
        updateBottomBadge();
    }

    // =====================================================================
    // 🔥 Actualización del badge del menú inferior en tiempo real
    // =====================================================================
    private void updateBottomBadge() {

        int totalUnread = 0;

        for (ChatPreview c : chatList) totalUnread += c.getUnreadCount();

        // Update MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateChatBadge(totalUnread);
        }

        BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
        BadgeDrawable badge = nav.getOrCreateBadge(R.id.nav_chat);

        if (totalUnread > 0) {
            badge.setVisible(true);
            badge.setNumber(totalUnread);
        } else {
            badge.setVisible(false);
        }
    }

    // =====================================================================
    // 🔥 Eliminar listeners al cerrar fragment
    // =====================================================================
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (chatListener != null) chatListener.remove();
        for (ListenerRegistration l : messageListeners.values()) l.remove();
        messageListeners.clear();
    }
}
