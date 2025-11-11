package com.tzolas.camisetaswallapop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.ChatAdapter;
import com.tzolas.camisetaswallapop.adapters.ChatRequestAdapter;

import com.tzolas.camisetaswallapop.models.ChatItem;
import com.tzolas.camisetaswallapop.models.ChatRequest;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView rvChats, rvSolicitudes;
    private ChatAdapter chatAdapter;
    private ChatRequestAdapter requestsAdapter;

    private List<ChatItem> chatItems = new ArrayList<>();
    private List<ChatRequest> requestItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChats = view.findViewById(R.id.rvChats);
        rvSolicitudes = view.findViewById(R.id.rvSolicitudes);

        // Chats vertical
        chatAdapter = new ChatAdapter(chatItems);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(chatAdapter);

        // Solicitudes horizontal
        requestsAdapter = new ChatRequestAdapter(requestItems);
        LinearLayoutManager horizontalLayout = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvSolicitudes.setLayoutManager(horizontalLayout);
        rvSolicitudes.setAdapter(requestsAdapter);

        // Datos de prueba
        loadDummyData();

        return view;
    }

    private void loadDummyData() {
        requestItems.add(new ChatRequest("1", "Ana", ""));
        requestItems.add(new ChatRequest("2", "Luis", ""));
        requestItems.add(new ChatRequest("3", "Marta", ""));
        requestsAdapter.notifyDataSetChanged();

        chatItems.add(new ChatItem("1", "Carlos", "Hola, ¿todo bien?", ""));
        chatItems.add(new ChatItem("2", "Sofía", "Gracias por la camiseta!", ""));
        chatItems.add(new ChatItem("3", "Pedro", "Cuando envías el pedido?", ""));
        chatAdapter.notifyDataSetChanged();
    }
}
