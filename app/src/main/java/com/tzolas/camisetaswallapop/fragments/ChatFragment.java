package com.tzolas.camisetaswallapop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ChatListAdapter;
import com.tzolas.camisetaswallapop.models.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recycler;
    private ChatListAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String myUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        recycler = v.findViewById(R.id.recyclerChats);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        myUid = FirebaseAuth.getInstance().getUid();

        loadChats();

        return v;
    }

    private void loadChats() {

        chatList.clear();

        db.collection("chats")
                .whereEqualTo("user1", myUid)
                .get()
                .addOnSuccessListener(q1 -> {

                    chatList.addAll(q1.toObjects(Chat.class));

                    db.collection("chats")
                            .whereEqualTo("user2", myUid)
                            .get()
                            .addOnSuccessListener(q2 -> {

                                chatList.addAll(q2.toObjects(Chat.class));

                                adapter = new ChatListAdapter(chatList);
                                recycler.setAdapter(adapter);
                            });
                });
    }
}

