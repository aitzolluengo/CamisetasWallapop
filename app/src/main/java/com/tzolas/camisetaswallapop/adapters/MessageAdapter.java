package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private final List<Message> messages;
    private final String myUid;

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
        if (type == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_right, parent, false);
            return new SentHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_left, parent, false);
            return new ReceivedHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        Message m = messages.get(pos);

        if (holder instanceof SentHolder) {
            SentHolder h = (SentHolder) holder;

            h.txtMessage.setText(m.getText());
            h.txtTime.setText(formatTime(m.getTimestamp()));

            int lastSentIndex = -1;
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).getSenderId().equals(myUid)) {
                    lastSentIndex = i;
                    break;
                }
            }

            if (pos == lastSentIndex) {
                h.txtStatusText.setVisibility(View.VISIBLE);

                if (m.isRead()) {
                    h.txtStatusText.setText("Leído");
                    h.txtStatusText.setTextColor(0xFF5CC8FF);
                } else if (m.isDelivered()) {
                    h.txtStatusText.setText("Entregado");
                    h.txtStatusText.setTextColor(0xFFBDBDBD);
                } else {
                    h.txtStatusText.setText("Enviado");
                    h.txtStatusText.setTextColor(0xFF888888);
                }

            } else {
                h.txtStatusText.setVisibility(View.GONE);
            }

        } else {
            ReceivedHolder h = (ReceivedHolder) holder;
            h.txtMessage.setText(m.getText());
            h.txtTime.setText(formatTime(m.getTimestamp()));
        }

        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(120).start();
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm").format(new Date(timestamp));
    }

    // ==========================================
    // HOLDERS
    // ==========================================

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime, txtStatusText;

        public SentHolder(@NonNull View item) {
            super(item);
            txtMessage = item.findViewById(R.id.txtMessageRight);
            txtTime = item.findViewById(R.id.txtTimeRight);
            txtStatusText = item.findViewById(R.id.txtStatusText);
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
