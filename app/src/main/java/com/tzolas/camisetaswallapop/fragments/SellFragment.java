package com.tzolas.camisetaswallapop.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.ProductRepository;
import com.tzolas.camisetaswallapop.utils.CloudinaryUploader;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellFragment extends Fragment {

    private ImageView imagePreview;
    private Button btnSelectImage, btnPublish;

    private EditText editTitle, editPrice, editDescription;
    private Spinner spinnerCategory;

    /// CAMPOS EXTRA
    private View layoutExtraCromo, layoutExtraCamiseta, layoutExtraEntrada;

    // CROMO
    private EditText editJugador, editAno, editEdicion;

    // CAMISETA
    private EditText editEquipo, editTemporada;
    private Spinner spinnerTalla;

    // ENTRADA
    private EditText editEstadio, editZona;
    private Button btnPickFecha;
    private TextView tvFechaSeleccionada;
    private long fechaTimestamp = 0;

    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        imagePreview = view.findViewById(R.id.imagePreview);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnPublish = view.findViewById(R.id.btnPublish);
        editTitle = view.findViewById(R.id.editTitle);
        editPrice = view.findViewById(R.id.editPrice);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);

        // EXTRA
        findExtraViews(view);
        setupCategorySpinner();
        setupImagePicker();
        setupTallaSpinner();
        setupDatePicker();

        btnPublish.setOnClickListener(v -> publishProduct());

        return view;
    }

    private void findExtraViews(View view) {
        layoutExtraCromo = view.findViewById(R.id.layoutExtraCromo);
        layoutExtraCamiseta = view.findViewById(R.id.layoutExtraCamiseta);
        layoutExtraEntrada = view.findViewById(R.id.layoutExtraEntrada);

        // CROMO
        editJugador = view.findViewById(R.id.editJugador);
        editAno = view.findViewById(R.id.editAno);
        editEdicion = view.findViewById(R.id.editEdicion);

        // CAMISETA
        editEquipo = view.findViewById(R.id.editEquipo);
        editTemporada = view.findViewById(R.id.editTemporada);
        spinnerTalla = view.findViewById(R.id.spinnerTalla);

        // ENTRADA
        editEstadio = view.findViewById(R.id.editEstadio);
        editZona = view.findViewById(R.id.editZona);
        btnPickFecha = view.findViewById(R.id.btnPickFecha);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
    }

    private void setupCategorySpinner() {
        String[] categories = {"cromo", "camiseta", "entrada"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = spinnerCategory.getSelectedItem().toString();
                showCategoryUI(selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showCategoryUI(String cat) {
        layoutExtraCromo.setVisibility(View.GONE);
        layoutExtraCamiseta.setVisibility(View.GONE);
        layoutExtraEntrada.setVisibility(View.GONE);

        switch (cat) {
            case "cromo":
                layoutExtraCromo.setVisibility(View.VISIBLE);
                break;

            case "camiseta":
                layoutExtraCamiseta.setVisibility(View.VISIBLE);
                break;

            case "entrada":
                layoutExtraEntrada.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupTallaSpinner() {
        String[] tallas = {"S", "M", "L", "XL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                tallas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTalla.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnPickFecha.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(getContext(), (DatePicker view, int year, int month, int day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                fechaTimestamp = selected.getTimeInMillis();
                tvFechaSeleccionada.setText(day + "/" + (month + 1) + "/" + year);
            }, y, m, d);

            dp.show();
        });
    }

    private void setupImagePicker() {
        btnSelectImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(pick, 100);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
        }
    }

    private void publishProduct() {

        Toast.makeText(getContext(), "Publicando...", Toast.LENGTH_SHORT).show();

        String title = editTitle.getText().toString();
        String priceStr = editPrice.getText().toString();
        String description = editDescription.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (imageUri == null || title.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        /// EXTRA DATA
        Map<String, Object> extra = new HashMap<>();

        switch (category) {

            case "cromo":
                extra.put("jugador", editJugador.getText().toString());
                extra.put("año", editAno.getText().toString());
                extra.put("edicion", editEdicion.getText().toString());
                break;

            case "camiseta":
                extra.put("equipo", editEquipo.getText().toString());
                extra.put("talla", spinnerTalla.getSelectedItem().toString());
                extra.put("temporada", editTemporada.getText().toString());
                break;

            case "entrada":
                extra.put("estadio", editEstadio.getText().toString());
                extra.put("zona", editZona.getText().toString());
                extra.put("fecha", fechaTimestamp);
                break;
        }

        Product product = new Product(
                productId,
                title,
                category,
                price,
                description,
                "",
                user.getUid(),
                timestamp,
                extra
        );

        // ⬇ SUBIMOS EN HILO
        new Thread(() -> {

            CloudinaryUploader uploader = new CloudinaryUploader();
            String cloudURL = uploader.uploadImage(requireContext(), imageUri);

            if (cloudURL == null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "❌ Error al subir imagen", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            product.setImageUrl(cloudURL);

            ProductRepository repo = new ProductRepository();
            repo.uploadProduct(product)
                    .addOnSuccessListener(unused -> {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "✅ Producto publicado!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    })
                    .addOnFailureListener(e -> {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "❌ Error al publicar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    });

        }).start();
    }
}
