package com.tzolas.camisetaswallapop.activities;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.fragments.BuscarFragment;
import com.tzolas.camisetaswallapop.fragments.ChatFragment;
import com.tzolas.camisetaswallapop.fragments.HomeFragment;
import com.tzolas.camisetaswallapop.fragments.PerfilFragment;
import com.tzolas.camisetaswallapop.fragments.SellFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Cargar fragment por defecto o según el intent inicial
        handleGoTo(getIntent());

        // Listener del bottom nav
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_sell) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SellFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.nav_inicio) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
            else if (item.getItemId() == R.id.nav_buscar) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BuscarFragment()).commit();
            }
            else if (item.getItemId() == R.id.nav_chat) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatFragment()).commit();
            }
            else if (item.getItemId() == R.id.nav_perfil) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PerfilFragment()).commit();
            }

            return true;
        });

        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chat);
        badge.setVisible(false); // oculto por defecto
        badge.setBackgroundColor(Color.parseColor("#C77DFF"));
        badge.setBadgeTextColor(Color.WHITE);

        fixChatsWithoutParticipants();
    }

    public void updateChatBadge(int count) {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        BadgeDrawable badge = nav.getOrCreateBadge(R.id.nav_chat);

        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.setVisible(false);
        }
    }

    private void fixChatsWithoutParticipants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chats")
                .get()
                .addOnSuccessListener(snap -> {

                    for (var doc : snap.getDocuments()) {

                        if (!doc.contains("participants")) {

                            String u1 = doc.getString("user1");
                            String u2 = doc.getString("user2");

                            if (u1 != null && u2 != null) {
                                doc.getReference().update("participants", Arrays.asList(u1, u2))
                                        .addOnSuccessListener(v ->
                                                Log.d("FIX_CHATS", "Arreglado chat: " + doc.getId())
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("FIX_CHATS", "Error en chat: " + doc.getId(), e)
                                        );
                            }
                        }
                    }

                    Log.d("FIX_CHATS", "Proceso completado");
                });
    }

    private void handleGoTo(Intent intent) {
        if (intent == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_inicio);
            return;
        }

        String goTo = intent.getStringExtra("goTo");
        Log.d("MainActivity", "handleGoTo -> " + goTo);

        if ("perfil".equals(goTo)) {
            loadFragment(new PerfilFragment());
            bottomNav.setSelectedItemId(R.id.nav_perfil);
        }
        else if ("vender".equals(goTo)) {
            loadFragment(new SellFragment());
            bottomNav.setSelectedItemId(R.id.nav_sell);
        }
        else {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ya no hace falta refrescar manualmente
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleGoTo(intent);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
