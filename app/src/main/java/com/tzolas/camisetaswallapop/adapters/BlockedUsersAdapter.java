package com.tzolas.camisetaswallapop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tzolas.camisetaswallapop.R;
import java.util.List;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder> {

    private List<String> blockedUsers;
    private OnUnblockListener onUnblockListener;

    public interface OnUnblockListener {
        void onUnblock(String userId);
    }

    public BlockedUsersAdapter(List<String> blockedUsers, OnUnblockListener listener) {
        this.blockedUsers = blockedUsers;
        this.onUnblockListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blocked_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userId = blockedUsers.get(position);
        holder.txtUserId.setText("Usuario: " + userId);

        holder.btnUnblock.setOnClickListener(v -> {
            if (onUnblockListener != null) {
                onUnblockListener.onUnblock(userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserId;
        Button btnUnblock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            btnUnblock = itemView.findViewById(R.id.btnUnblock);
        }
    }
}