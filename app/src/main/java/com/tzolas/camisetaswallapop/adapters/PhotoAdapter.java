package com.tzolas.camisetaswallapop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tzolas.camisetaswallapop.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<String> photoUris;  // pueden ser URIs locales o URLs Cloudinary
    private final OnPhotoActionListener listener;

    public interface OnPhotoActionListener {
        void onDelete(int position);
        void onSetMain(int position);
    }

    public PhotoAdapter(Context context, List<String> photoUris, OnPhotoActionListener listener) {
        this.context = context;
        this.photoUris = photoUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {

        String uri = photoUris.get(position);

        Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(holder.image);

        // ⭐ principal → si es posición 0, icono en dorado
        if (position == 0) {
            holder.btnStar.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.btnStar.setImageResource(R.drawable.ic_star_outline);
        }

        holder.btnStar.setOnClickListener(v -> listener.onSetMain(position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        ImageButton btnStar, btnDelete;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.photoImage);
            btnStar = itemView.findViewById(R.id.btnSetMain);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
