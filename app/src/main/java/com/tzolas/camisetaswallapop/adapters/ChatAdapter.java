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
import com.tzolas.camisetaswallapop.models.ChatItem;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatItem> items;

    public ChatAdapter(List<ChatItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = items.get(position);
        holder.name.setText(item.name);
        holder.message.setText(item.lastMessage);

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
        TextView name, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imgAvatar);
            name = itemView.findViewById(R.id.txtName);
            //message = itemView.findViewById(R.id.txtMessage);
        }
    }
}
