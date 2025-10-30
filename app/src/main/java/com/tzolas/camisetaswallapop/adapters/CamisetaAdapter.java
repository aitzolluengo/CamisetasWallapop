package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Camiseta;
import java.util.List;

public class CamisetaAdapter extends RecyclerView.Adapter<CamisetaAdapter.ViewHolder> {

    private Context context;
    private List<Camiseta> lista;

    public CamisetaAdapter(Context context, List<Camiseta> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_camiseta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Camiseta c = lista.get(position);
        holder.tvEquipo.setText(c.getEquipo());
        holder.tvTalla.setText("Talla: " + c.getTalla());
        holder.tvPrecio.setText(c.getPrecio() + " €");
        holder.imgCamiseta.setImageResource(c.getImagen());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvTalla, tvPrecio;
        ImageView imgCamiseta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvTalla = itemView.findViewById(R.id.tvTalla);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            imgCamiseta = itemView.findViewById(R.id.imgCamiseta);
        }
    }
}
