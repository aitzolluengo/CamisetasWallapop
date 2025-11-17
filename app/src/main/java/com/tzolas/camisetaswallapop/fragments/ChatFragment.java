package com.tzolas.camisetaswallapop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ChatListAdapter;
import com.tzolas.camisetaswallapop.models.Chat;
import com.tzolas.camisetaswallapop.models.ChatPreview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recycler;
    private ChatListAdapter adapter;
    private final List<ChatPreview> chatList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String myUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        recycler = v.findViewById(R.id.recyclerChats);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        myUid = FirebaseAuth.getInstance().getUid();

        adapter = new ChatListAdapter(chatList);
        recycler.setAdapter(adapter);

        loadChats();

        return v;
    }

    private void loadChats() {
        if (myUid == null) return;

        chatList.clear();

        db.collection("chats")
                .whereArrayContains("participants", myUid)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) return;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat == null) continue;

                        final String chatId = chat.getId();
                        final String otherUid = chat.getUser1().equals(myUid) ? chat.getUser2() : chat.getUser1();

                        // 🔹 Obtener el último mensaje
                        db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(messages -> {
                                    final String lastMsg;
                                    final long lastTime;

                                    if (!messages.isEmpty()) {
                                        DocumentSnapshot m = messages.getDocuments().get(0);
                                        lastMsg = m.getString("text") != null ? m.getString("text") : "Mensaje";
                                        Long ts = m.getLong("timestamp");
                                        lastTime = ts != null ? ts : 0L;
                                    } else {
                                        lastMsg = "Nuevo chat";
                                        lastTime = 0L;
                                    }

                                    // 🔹 Obtener datos del otro usuario
                                    db.collection("users").document(otherUid).get()
                                            .addOnSuccessListener(userDoc -> {
                                                String name = userDoc.getString("name");
                                                String photo = userDoc.getString("photo");

                                                ChatPreview preview = new ChatPreview(
                                                        chatId,
                                                        otherUid,
                                                        (name != null && !name.isEmpty()) ? name : "Usuario",
                                                        photo,
                                                        lastMsg,
                                                        lastTime,
                                                        chat.getProductId()
                                                );

                                                chatList.add(preview);
                                                chatList.sort(Comparator.comparingLong(ChatPreview::getLastMessageTime).reversed());
                                                adapter.notifyDataSetChanged();
                                            });
                                });
                    }
                });
    }
}