package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ChatActivity;
import com.tzolas.camisetaswallapop.models.ChatPreview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<ChatPreview> chats;
    private Context context;

    public ChatListAdapter(List<ChatPreview> chats) {
        this.chats = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatPreview chat = chats.get(position);

        holder.txtName.setText(chat.getOtherUserName());
        holder.txtLastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "Nuevo chat");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        holder.txtTime.setText(chat.getLastMessageTime() != 0 ? sdf.format(new Date(chat.getLastMessageTime())) : "");

        Glide.with(context)
                .load(chat.getOtherUserPhoto())
                .placeholder(R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(holder.imgUser);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("chatId", chat.getChatId());
            i.putExtra("sellerId", chat.getOtherUserId());
            i.putExtra("productId" , chat.getProductId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUser;
        TextView txtName, txtLastMessage, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUser);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}