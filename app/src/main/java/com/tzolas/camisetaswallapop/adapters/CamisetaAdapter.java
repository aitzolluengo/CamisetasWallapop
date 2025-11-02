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
import com.tzolas.camisetaswallapop.models.Camiseta;

import java.util.List;

public class CamisetaAdapter extends RecyclerView.Adapter<CamisetaAdapter.ViewHolder> {

    private Context context;
    private List<Camiseta> camisetaList;

    public CamisetaAdapter(Context context, List<Camiseta> camisetaList) {
        this.context = context;
        this.camisetaList = camisetaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_camiseta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Camiseta camiseta = camisetaList.get(position);
        holder.tvNombre.setText(camiseta.getNombre());
        holder.tvPrecio.setText(camiseta.getPrecio() + " €");

        Glide.with(context)
                .load(camiseta.getImagenUrl())
                .placeholder(R.drawable.placeholder_camiseta)
                .into(holder.ivCamiseta);
    }

    @Override
    public int getItemCount() {
        return camisetaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCamiseta;
        TextView tvNombre, tvPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCamiseta = itemView.findViewById(R.id.ivCamiseta);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
        }
    }
}
