package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Product> products;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductsAdapter(Context context, List<Product> products, OnItemClickListener listener) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.listener = listener;
    }

    public ProductsAdapter(Context context, List<Product> products) {
        this(context, products, null);
    }

    public ProductsAdapter(List<Product> products) {
        this(null, products, null);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);

        holder.title.setText(p.getTitle());
        holder.price.setText(p.getPrice() + "€");

        Glide.with(holder.itemView.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.bg_image_placeholder)
                .into(holder.image);

        // --- Lógica de VENDIDO ---
        if (holder.badgeSold != null) {
            if (p.isSold()) {
                holder.badgeSold.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha(0.6f);
                holder.itemView.setOnClickListener(null); // desactivar click si está vendido
            } else {
                holder.badgeSold.setVisibility(View.GONE);
                holder.itemView.setAlpha(1f);
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(p);
                });
            }
        } else {
            // Si no tienes badge en el layout, al menos respeta el click
            holder.itemView.setAlpha(p.isSold() ? 0.6f : 1f);
            if (p.isSold()) {
                holder.itemView.setOnClickListener(null);
            } else {
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(p);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, badgeSold;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            price = itemView.findViewById(R.id.txtPrice);
            image = itemView.findViewById(R.id.imgProduct);
            // Este id es opcional; si no existe en tu XML, quedará null y no pasa nada
            badgeSold = itemView.findViewById(R.id.badgeSold);
        }
    }
}
