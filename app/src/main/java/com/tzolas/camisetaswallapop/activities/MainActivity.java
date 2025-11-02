package com.tzolas.camisetaswallapop.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.fragments.BuscarFragment;
import com.tzolas.camisetaswallapop.fragments.ChatFragment;
import com.tzolas.camisetaswallapop.fragments.HomeFragment;
import com.tzolas.camisetaswallapop.fragments.PerfilFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) selected = new HomeFragment();
            else if (id == R.id.nav_buscar) selected = new BuscarFragment();
            else if (id == R.id.nav_chat) selected = new ChatFragment();
            else if (id == R.id.nav_perfil) selected = new PerfilFragment();

            if (selected != null)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();

            return true;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
