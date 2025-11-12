package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String myUid;

    public MessageAdapter(List<Message> messages, String myUid) {
        this.messages = messages;
        this.myUid = myUid;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(myUid)
                ? TYPE_SENT
                : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {

        View view;

        if (type == TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_right, parent, false);
            return new SentHolder(view);

        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_left, parent, false);
            return new ReceivedHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        Message m = messages.get(pos);

        if (holder instanceof SentHolder) {
            ((SentHolder) holder).txtMessage.setText(m.getText());
            ((SentHolder) holder).txtTime.setText(formatTime(m.getTimestamp()));
        } else {
            ((ReceivedHolder) holder).txtMessage.setText(m.getText());
            ((ReceivedHolder) holder).txtTime.setText(formatTime(m.getTimestamp()));
        }

        // 🔥 animación de aparición
        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(150).start();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        public SentHolder(@NonNull View item) {
            super(item);
            txtMessage = item.findViewById(R.id.txtMessageRight);
            txtTime = item.findViewById(R.id.txtTimeRight);
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        public ReceivedHolder(@NonNull View item) {
            super(item);
            txtMessage = item.findViewById(R.id.txtMessageLeft);
            txtTime = item.findViewById(R.id.txtTimeLeft);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

}
