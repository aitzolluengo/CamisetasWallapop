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
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) selected = new HomeFragment();
            else if (id == R.id.nav_buscar) selected = new BuscarFragment();
            else if (id == R.id.nav_chat) selected = new ChatFragment();
            else if (id == R.id.nav_perfil) selected = new PerfilFragment();

            if (selected != null)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();

            return true;
        });
    }

    // Este método centraliza la lógica de navegación según el Intent (goTo)
    private void handleGoTo(Intent intent) {
        if (intent == null) {
            // por seguridad, carga home si no hay intent
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_inicio);
            return;
        }

        String goTo = intent.getStringExtra("goTo");
        Log.d("MainActivity", "handleGoTo -> " + goTo);
        if ("perfil".equals(goTo)) {
            loadFragment(new PerfilFragment());
            bottomNav.setSelectedItemId(R.id.nav_perfil);
        } else {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        }
    }

    // Crucial: cuando la Activity ya existe y se le trae al frente, Android llamará a onNewIntent()
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // actualiza el intent interno
        handleGoTo(intent); // vuelve a manejar la navegación
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
