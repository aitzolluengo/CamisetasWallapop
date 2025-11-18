package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private static final int TYPE_OFFER_SENT = 10;     //  oferta enviada derecha
    private static final int TYPE_OFFER_RECEIVED = 11; //  oferta recibida izquierda

    private final List<Message> messages;
    private final String myUid;

    // === INTERFAZ PARA OFERTAS ===
    public interface OnOfferActionListener {
        void onAccept(Message m);
        void onReject(Message m);
    }

    private final OnOfferActionListener offerListener;

    public MessageAdapter(List<Message> messages, String myUid, OnOfferActionListener listener) {
        this.messages = messages;
        this.myUid = myUid;
        this.offerListener = listener;
    }

    // TIPOS DE VISTA
    @Override
    public int getItemViewType(int position) {
        Message m = messages.get(position);

        if ("offer".equals(m.getType())) {
            if (m.getSenderId().equals(myUid)) {
                return TYPE_OFFER_SENT;     // oferta derecha
            } else {
                return TYPE_OFFER_RECEIVED; // oferta izquierda
            }
        }

        return m.getSenderId().equals(myUid) ? TYPE_SENT : TYPE_RECEIVED;
    }

    // CREAR VISTA
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (type) {

            case TYPE_OFFER_SENT:
                return new OfferSentHolder(
                        inflater.inflate(R.layout.item_msg_offer_right, parent, false)
                );

            case TYPE_OFFER_RECEIVED:
                return new OfferReceivedHolder(
                        inflater.inflate(R.layout.item_msg_offer_left, parent, false)
                );

            case TYPE_SENT:
                return new SentHolder(
                        inflater.inflate(R.layout.item_msg_right, parent, false)
                );

            default:
                return new ReceivedHolder(
                        inflater.inflate(R.layout.item_msg_left, parent, false)
                );
        }
    }

    // BIND
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        Message m = messages.get(pos);

        //  OFERTA ENVIADA (derecha)
        if (holder instanceof OfferSentHolder) {

            OfferSentHolder h = (OfferSentHolder) holder;

            h.txtOffer.setText("Oferta: " + m.getOfferPrice() + " " + "puntos");
            h.txtTime.setText(formatTime(m.getTimestamp()));

            switch (m.getStatus()) {
                case "pending":
                    h.txtStatus.setText("Pendiente…");
                    h.txtStatus.setTextColor(0xFF888888);
                    break;

                case "accepted":
                    h.txtStatus.setText("Aceptada ✔");
                    h.txtStatus.setTextColor(0xFF4CAF50);
                    break;

                case "rejected":
                    h.txtStatus.setText("Rechazada ✖");
                    h.txtStatus.setTextColor(0xFFF44336);
                    break;
            }

            return;
        }

        // OFERTA RECIBIDA (izquierda)
        if (holder instanceof OfferReceivedHolder) {

            OfferReceivedHolder h = (OfferReceivedHolder) holder;

            h.txtOffer.setText("Oferta: " + m.getOfferPrice() + " " + "puntos");
            h.txtTime.setText(formatTime(m.getTimestamp()));

            if ("pending".equals(m.getStatus())) {
                h.layoutButtons.setVisibility(View.VISIBLE);

                h.btnAccept.setOnClickListener(v -> {
                    if (offerListener != null) offerListener.onAccept(m);
                });

                h.btnReject.setOnClickListener(v -> {
                    if (offerListener != null) offerListener.onReject(m);
                });

            } else {
                h.layoutButtons.setVisibility(View.GONE);
            }

            return;
        }

        //  MENSAJE NORMAL ENVIADO
        if (holder instanceof SentHolder) {

            SentHolder h = (SentHolder) holder;

            h.txtMessage.setText(m.getText());
            h.txtTime.setText(formatTime(m.getTimestamp()));

            int lastSent = getLastSentIndex();

            if (pos == lastSent) {
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

            return;
        }

        //  MENSAJE NORMAL RECIBIDO
        if (holder instanceof ReceivedHolder) {

            ReceivedHolder h = (ReceivedHolder) holder;

            h.txtMessage.setText(m.getText());
            h.txtTime.setText(formatTime(m.getTimestamp()));
        }
    }

    // HELPERS
    private int getLastSentIndex() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getSenderId().equals(myUid)) return i;
        }
        return -1;
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm").format(new Date(timestamp));
    }

    // HOLDERS

    static class OfferSentHolder extends RecyclerView.ViewHolder {
        TextView txtOffer, txtTime, txtStatus;

        public OfferSentHolder(@NonNull View v) {
            super(v);
            txtOffer = v.findViewById(R.id.txtOfferRight);
            txtTime = v.findViewById(R.id.txtTimeRight);
            txtStatus = v.findViewById(R.id.txtStatusRight);
        }
    }

    // --- Oferta recibida (izquierda)
    static class OfferReceivedHolder extends RecyclerView.ViewHolder {
        TextView txtOffer, txtTime;
        LinearLayout layoutButtons;
        Button btnAccept, btnReject;

        public OfferReceivedHolder(@NonNull View v) {
            super(v);
            txtOffer = v.findViewById(R.id.txtOfferLeft);
            txtTime = v.findViewById(R.id.txtTimeLeft);
            layoutButtons = v.findViewById(R.id.layoutOfferButtons);
            btnAccept = v.findViewById(R.id.btnAcceptOffer);
            btnReject = v.findViewById(R.id.btnRejectOffer);
        }
    }

    // --- mensaje enviado
    static class SentHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime, txtStatusText;

        public SentHolder(@NonNull View v) {
            super(v);
            txtMessage = v.findViewById(R.id.txtMessageRight);
            txtTime = v.findViewById(R.id.txtTimeRight);
            txtStatusText = v.findViewById(R.id.txtStatusText);
        }
    }

    // --- mensaje recibido
    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        public ReceivedHolder(@NonNull View v) {
            super(v);
            txtMessage = v.findViewById(R.id.txtMessageLeft);
            txtTime = v.findViewById(R.id.txtTimeLeft);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
