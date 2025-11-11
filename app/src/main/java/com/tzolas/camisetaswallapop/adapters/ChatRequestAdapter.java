package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.ChatRequest;

import java.util.List;

public class ChatRequestAdapter extends RecyclerView.Adapter<ChatRequestAdapter.ViewHolder> {

    private List<ChatRequest> items;

    public ChatRequestAdapter(List<ChatRequest> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRequest item = items.get(position);
        holder.name.setText(item.name);

        Glide.with(holder.itemView.getContext())
                .load(item.avatarUrl)
                .placeholder(R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imgAvatar);
            name = itemView.findViewById(R.id.txtName);
        }
    }
}
