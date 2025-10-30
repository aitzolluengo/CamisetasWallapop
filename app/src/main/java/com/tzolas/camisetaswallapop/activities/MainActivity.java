package com.tzolas.camisetaswallapop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.CamisetaAdapter;
import com.tzolas.camisetaswallapop.models.Camiseta;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnAdd;
    List<Camiseta> lista;
    CamisetaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerCamisetas);
        btnAdd = findViewById(R.id.btnAdd);

        lista = new ArrayList<>();
        lista.add(new Camiseta("Athletic Club", "L", 25, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Real Madrid", "M", 30, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Barcelona", "S", 28, R.drawable.ic_launcher_background));
        lista.add(new Camiseta("Betis", "L", 22, R.drawable.ic_launcher_background));

        adapter = new CamisetaAdapter(this, lista);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddCamisetaActivity.class));
        });
    }
}
