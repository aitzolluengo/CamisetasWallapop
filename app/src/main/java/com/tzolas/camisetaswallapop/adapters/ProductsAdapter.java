package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.util.Log;
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
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Product> products;
    private final Context context;
    private OnItemClickListener listener;

    private final String myUid = FirebaseAuth.getInstance().getUid();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // -------------------------
    // Callbacks
    // -------------------------
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // Notifica al fragment/actividad que cambió el favorito (se pasa el product con el nuevo estado)
    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(Product product);
    }

    private OnFavoriteChangeListener favListener;

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.favListener = listener;
    }

    // -------------------------
    // Constructor
    // -------------------------
    public ProductsAdapter(Context context, List<Product> products, OnItemClickListener listener) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.listener = listener;
    }

    // ✅ actualizar productos desde fuera (SearchResultsActivity y Home)
    public void updateProducts(List<Product> newList) {
        this.products = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = context != null ? context : parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Product p = products.get(position);

        h.title.setText(p.getTitle());
        h.price.setText(p.getPrice() + "€");

        Glide.with(h.itemView.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.bg_image_placeholder)
                .into(h.image);

        // ❤️ FAVORITO: icono según estado
        h.btnFavorite.setImageResource(
                p.isFavorite()
                        ? R.drawable.ic_heart_filled
                        : R.drawable.ic_heart_empty
        );

        // ❤️ CLICK FAVORITO + animación
        h.btnFavorite.setOnClickListener(v -> toggleFavorite(p, h));

        // CLICK ITEM (si no está vendido)
        if (!p.isSold()) {
            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(p);
            });
        }

        // BADGE VENDIDO
        if (h.badgeSold != null) {
            h.badgeSold.setVisibility(p.isSold() ? View.VISIBLE : View.GONE);
            h.itemView.setAlpha(p.isSold() ? 0.6f : 1f);
        }
    }

    /**
     * Cambia el estado local del favorito (optimista), intenta escribir en Firestore
     * y notifica al listener inmediatamente. Si la escritura falla, revierte y notifica otra vez.
     */
    private void toggleFavorite(Product p, ViewHolder h) {
        if (myUid == null) return;

        boolean newState = !p.isFavorite();

        // Cambio optimista local y UI
        p.setFavorite(newState);
        h.btnFavorite.animate().scaleX(0.6f).scaleY(0.6f).setDuration(100).withEndAction(() -> {
            h.btnFavorite.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_empty);
            h.btnFavorite.animate().scaleX(1f).scaleY(1f).setDuration(120);
        });

        // Notificar inmediatamente para que el fragment actualice sus listas
        if (favListener != null) {
            try {
                favListener.onFavoriteChanged(p);
            } catch (Exception ex) {
                Log.w("ProductsAdapter", "favListener threw: " + ex.getMessage());
            }
        }

        // Persistir en Firestore
        if (newState) {
            db.collection("users")
                    .document(myUid)
                    .collection("favorites")
                    .document(p.getId())
                    .set(Collections.singletonMap("timestamp", System.currentTimeMillis()))
                    .addOnFailureListener(e -> {
                        // Revertir en caso de fallo y notificar
                        p.setFavorite(!newState);
                        notifyItemChanged(indexOfProduct(p));
                        if (favListener != null) favListener.onFavoriteChanged(p);
                        Log.w("ProductsAdapter", "Failed to add favorite: " + e.getMessage());
                    });
        } else {
            db.collection("users")
                    .document(myUid)
                    .collection("favorites")
                    .document(p.getId())
                    .delete()
                    .addOnFailureListener(e -> {
                        // Revertir en caso de fallo y notificar
                        p.setFavorite(!newState);
                        notifyItemChanged(indexOfProduct(p));
                        if (favListener != null) favListener.onFavoriteChanged(p);
                        Log.w("ProductsAdapter", "Failed to remove favorite: " + e.getMessage());
                    });
        }
    }

    // helper: buscar índice del product en la lista actual
    private int indexOfProduct(Product p) {
        if (products == null) return -1;
        for (int i = 0; i < products.size(); i++) {
            Product x = products.get(i);
            if (x.getId() != null && x.getId().equals(p.getId())) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, badgeSold;
        ImageView image, btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            price = itemView.findViewById(R.id.txtPrice);
            image = itemView.findViewById(R.id.imgProduct);
            badgeSold = itemView.findViewById(R.id.badgeSold);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
