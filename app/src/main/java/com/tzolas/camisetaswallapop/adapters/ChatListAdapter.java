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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.activities.ChatActivity;
import com.tzolas.camisetaswallapop.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chats;

    public ChatListAdapter(List<Chat> chats) {
        this.chats = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Chat c = chats.get(pos);

        String myId = FirebaseAuth.getInstance().getUid();
        String otherId = c.getUser1().equals(myId) ? c.getUser2() : c.getUser1();

        loadUserInfo(otherId, h);

        // Cargar último mensaje
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(c.getId())
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        var msg = q.getDocuments().get(0);
                        h.txtLastMessage.setText(msg.getString("text"));

                        long ts = msg.getLong("timestamp");
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(new Date(ts));
                        h.txtTime.setText(time);
                    }
                });

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("chatId", c.getId());
            i.putExtra("sellerId", otherId);    // lo tratamos como la otra persona
            i.putExtra("productId", c.getProductId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgUser;
        TextView txtUserName, txtLastMessage, txtTime;

        public ViewHolder(@NonNull View item) {
            super(item);
            imgUser       = item.findViewById(R.id.imgUser);
            txtUserName   = item.findViewById(R.id.txtUserName);
            txtLastMessage = item.findViewById(R.id.txtLastMessage);
            txtTime       = item.findViewById(R.id.txtTime);
        }
    }

    /** ---------------------------------------------------------
     * ✅ Load other user info
     * --------------------------------------------------------- */
    private void loadUserInfo(String otherId, ViewHolder h) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherId)
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.getString("name");
                    String photo = doc.getString("photo");

                    h.txtUserName.setText(
                            (name != null && !name.isEmpty())
                                    ? name
                                    : "Usuario"
                    );

                    if (photo != null && !photo.isEmpty()) {
                        Glide.with(context)
                                .load(photo)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(h.imgUser);
                    } else {
                        h.imgUser.setImageResource(R.drawable.ic_user_placeholder);
                    }
                });
    }
}
