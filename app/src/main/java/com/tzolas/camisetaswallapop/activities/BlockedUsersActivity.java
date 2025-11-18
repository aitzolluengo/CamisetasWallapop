package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.BlockedUsersAdapter;
import com.tzolas.camisetaswallapop.repositories.SecurityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockedUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BlockedUsersAdapter adapter;
    private SecurityRepository securityRepository;
    private List<String> blockedUsersList = new ArrayList<>();
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        securityRepository = new SecurityRepository(this);
        recyclerView = findViewById(R.id.recyclerBlockedUsers);
        txtEmpty = findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlockedUsersAdapter(blockedUsersList, userId -> {
            // Desbloquear usuario
            securityRepository.unblockUser(userId);
            loadBlockedUsers();
            Toast.makeText(this, "Usuario desbloqueado", Toast.LENGTH_SHORT).show();

            // NUEVO: Notificar que se desbloqueó un usuario
            Intent broadcastIntent = new Intent("USER_UNBLOCKED");
            sendBroadcast(broadcastIntent);
        });
        recyclerView.setAdapter(adapter);

        loadBlockedUsers();
    }

    private void loadBlockedUsers() {
        Set<String> blockedUsers = securityRepository.getBlockedUsers();
        blockedUsersList.clear();
        blockedUsersList.addAll(blockedUsers);
        adapter.notifyDataSetChanged();

        if (blockedUsersList.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}