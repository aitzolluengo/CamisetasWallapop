package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tzolas.camisetaswallapop.R;

import java.util.List;
import java.util.Map;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {

    private List<Map<String, Object>> offers;
    private OnOfferClickListener listener;

    public interface OnOfferClickListener {
        void onOfferClick(Map<String, Object> offer);
    }

    public OffersAdapter(List<Map<String, Object>> offers, OnOfferClickListener listener) {
        this.offers = offers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OffersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OffersAdapter.ViewHolder holder, int position) {
        Map<String, Object> offer = offers.get(position);

        holder.title.setText((String) offer.get("productTitle"));
        holder.price.setText("Oferta: " + offer.get("price") + "puntos");
        holder.buyer.setText("Comprador: " + offer.get("buyerEmail"));

        holder.itemView.setOnClickListener(v -> listener.onOfferClick(offer));
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, buyer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtOfferTitle);
            price = itemView.findViewById(R.id.txtOfferPrice);
            buyer = itemView.findViewById(R.id.txtOfferBuyer);
        }
    }
}

