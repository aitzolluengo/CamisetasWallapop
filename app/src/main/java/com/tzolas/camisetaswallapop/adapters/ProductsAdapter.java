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

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Product> products;
    private Context context;

    public ProductsAdapter(List<Product> products) {
        this.products = products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product p = products.get(position);

        holder.title.setText(p.getTitle());
        holder.price.setText(p.getPrice() + "€");

        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.bg_image_placeholder) // opcional
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, price;
        ImageView image;

        public ViewHolder(@NonNull View item) {
            super(item);

            title = item.findViewById(R.id.txtTitle);
            price = item.findViewById(R.id.txtPrice);
            image = item.findViewById(R.id.imgProduct);
        }
    }
}
