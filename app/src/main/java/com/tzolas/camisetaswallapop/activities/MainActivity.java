package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.fragments.BuscarFragment;
import com.tzolas.camisetaswallapop.fragments.ChatFragment;
import com.tzolas.camisetaswallapop.fragments.HomeFragment;
import com.tzolas.camisetaswallapop.fragments.PerfilFragment;
import com.tzolas.camisetaswallapop.fragments.SellFragment; // ✅ IMPORTADO

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


    }

    // Este método centraliza la lógica de navegación según el Intent (goTo)
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
        // (Opcional si deseas abrir a vender desde intent en algún momento)
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
