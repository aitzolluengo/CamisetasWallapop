package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import java.util.Locale;

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

        int unread = chat.getUnreadCount();
        boolean hasUnread = unread > 0;

        // --------------------
        // 📌 NOMBRE
        // --------------------
        holder.txtName.setText(chat.getOtherUserName());
        holder.txtName.setTypeface(null, hasUnread ? Typeface.BOLD : Typeface.NORMAL);

        // --------------------
        // 📌 ÚLTIMO MENSAJE
        // --------------------
        if (chat.getLastMessage() != null) {
            holder.txtLastMessage.setText(chat.getLastMessage());
        } else {
            holder.txtLastMessage.setText("Nuevo chat");
        }
        holder.txtLastMessage.setTypeface(null, hasUnread ? Typeface.BOLD : Typeface.NORMAL);

        // --------------------
        // 📌 HORA
        // --------------------
        if (chat.getLastMessageTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.txtTime.setText(sdf.format(new Date(chat.getLastMessageTime())));
        } else {
            holder.txtTime.setText("");
        }

        // --------------------
        // 📌 FOTO
        // --------------------
        Glide.with(context)
                .load(chat.getOtherUserPhoto())
                .placeholder(R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(holder.imgUser);

        // --------------------
        // 📌 BADGE DE MENSAJES SIN LEER
        // --------------------
        if (hasUnread) {
            holder.txtUnreadBadge.setVisibility(View.VISIBLE);
            holder.txtUnreadBadge.setText(String.valueOf(unread));
        } else {
            holder.txtUnreadBadge.setVisibility(View.GONE);
        }

        // --------------------
        // 📌 CLICK → ABRIR CHAT
        // --------------------
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("chatId", chat.getChatId());
            i.putExtra("sellerId", chat.getOtherUserId());
            i.putExtra("productId", chat.getProductId());
            context.startActivity(i);
        });

        // --------------------
        // ✨ ANIMACIÓN SUAVE
        // --------------------
        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(150).start();
    }


    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUser;
        TextView txtName, txtLastMessage, txtTime, txtUnreadBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUser);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnreadBadge = itemView.findViewById(R.id.txtUnreadBadge);
        }
    }
}
