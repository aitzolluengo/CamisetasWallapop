package com.tzolas.camisetaswallapop.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.tzolas.camisetaswallapop.R;
import com.tzolas.camisetaswallapop.adapters.PhotoAdapter;
import com.tzolas.camisetaswallapop.models.Product;
import com.tzolas.camisetaswallapop.repositories.ProductRepository;
import com.tzolas.camisetaswallapop.utils.CloudinaryUploader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SellFragment extends Fragment {

    private static final int REQUEST_PHOTO_PICK = 3001;
    private static final int MAX_PHOTOS = 6;

    // UI
    private Button btnPublish;
    private MaterialCardView btnAddPhoto;

    private EditText editTitle, editPrice, editDescription;

    // Extras
    private View layoutExtraCromo, layoutExtraCamiseta, layoutExtraEntrada;
    private EditText editJugador, editAno, editEdicion;
    private EditText editEquipo, editTemporada;
    private Spinner spinnerTalla;
    private EditText editEstadio, editZona;
    private Button btnPickFecha;
    private TextView tvFechaSeleccionada;
    private long fechaTimestamp = 0;

    // Fotos
    private androidx.recyclerview.widget.RecyclerView recyclerPhotos;
    private PhotoAdapter photoAdapter;
    private final List<String> localPhotoUris = new ArrayList<>();

    private String selectedCategory = "camiseta";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        // UI
        btnPublish = view.findViewById(R.id.btnPublish);
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);

        editTitle = view.findViewById(R.id.editTitle);
        editPrice = view.findViewById(R.id.editPrice);
        editDescription = view.findViewById(R.id.editDescription);

        // FOTO SLIDER MINIATURAS
        recyclerPhotos = view.findViewById(R.id.recyclerPhotos);
        recyclerPhotos.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        photoAdapter = new PhotoAdapter(
                requireContext(),
                localPhotoUris,
                new PhotoAdapter.OnPhotoActionListener() {
                    @Override
                    public void onDelete(int position) {
                        localPhotoUris.remove(position);
                        photoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onSetMain(int position) {
                        if (position != 0) {
                            String selected = localPhotoUris.get(position);
                            localPhotoUris.remove(position);
                            localPhotoUris.add(0, selected);
                            photoAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        recyclerPhotos.setAdapter(photoAdapter);

        // Extras
        findExtraViews(view);
        setupTallaSpinner();
        setupDatePicker();

        // ADD PHOTO (+)
        btnAddPhoto.setOnClickListener(v -> openGalleryMulti());

        // Categorías
        view.findViewById(R.id.cardCamiseta).setOnClickListener(v -> {
            selectedCategory = "camiseta";
            showCategoryUI("camiseta");
        });

        view.findViewById(R.id.cardCromo).setOnClickListener(v -> {
            selectedCategory = "cromo";
            showCategoryUI("cromo");
        });

        view.findViewById(R.id.cardEntrada).setOnClickListener(v -> {
            selectedCategory = "entrada";
            showCategoryUI("entrada");
        });

        // Publicar
        btnPublish.setOnClickListener(v -> publishProduct());

        return view;
    }




    private void openGalleryMulti() {
        if (localPhotoUris.size() >= MAX_PHOTOS) {
            Toast.makeText(getContext(), "Máximo 6 fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pick.setType("image/*");
        startActivityForResult(Intent.createChooser(pick, "Seleccionar imágenes"), REQUEST_PHOTO_PICK);
    }

    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == REQUEST_PHOTO_PICK && res == Activity.RESULT_OK && data != null) {

            if (data.getClipData() != null) {

                int count = Math.min(
                        data.getClipData().getItemCount(),
                        MAX_PHOTOS - localPhotoUris.size()
                );

                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    localPhotoUris.add(uri.toString());
                }

            } else if (data.getData() != null) {

                if (localPhotoUris.size() < MAX_PHOTOS) {
                    localPhotoUris.add(data.getData().toString());
                }
            }

            photoAdapter.notifyDataSetChanged();
        }
    }


    private void findExtraViews(View view) {

        layoutExtraCromo = view.findViewById(R.id.layoutExtraCromo);
        layoutExtraCamiseta = view.findViewById(R.id.layoutExtraCamiseta);
        layoutExtraEntrada = view.findViewById(R.id.layoutExtraEntrada);

        editJugador = view.findViewById(R.id.editJugador);
        editAno = view.findViewById(R.id.editAno);
        editEdicion = view.findViewById(R.id.editEdicion);

        editEquipo = view.findViewById(R.id.editEquipo);
        editTemporada = view.findViewById(R.id.editTemporada);
        spinnerTalla = view.findViewById(R.id.spinnerTalla);

        editEstadio = view.findViewById(R.id.editEstadio);
        editZona = view.findViewById(R.id.editZona);

        btnPickFecha = view.findViewById(R.id.btnPickFecha);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
    }

    private void showCategoryUI(String cat) {
        layoutExtraCromo.setVisibility(View.GONE);
        layoutExtraCamiseta.setVisibility(View.GONE);
        layoutExtraEntrada.setVisibility(View.GONE);

        switch (cat) {
            case "cromo": layoutExtraCromo.setVisibility(View.VISIBLE); break;
            case "camiseta": layoutExtraCamiseta.setVisibility(View.VISIBLE); break;
            case "entrada": layoutExtraEntrada.setVisibility(View.VISIBLE); break;
        }
    }

    private void setupTallaSpinner() {
        String[] tallas = {"S", "M", "L", "XL"};

        android.widget.ArrayAdapter<String> adapter =
                new android.widget.ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        tallas
                );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTalla.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnPickFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    getContext(),
                    (DatePicker view, int year, int month, int day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day);
                        fechaTimestamp = selected.getTimeInMillis();
                        tvFechaSeleccionada.setText(day + "/" + (month + 1) + "/" + year);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void publishProduct() {

        if (localPhotoUris.isEmpty()) {
            Toast.makeText(getContext(), "Añade al menos 1 foto", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTitle.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        if (title.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> extra = new HashMap<>();

        switch (selectedCategory) {
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

        Toast.makeText(getContext(), "Subiendo imágenes...", Toast.LENGTH_SHORT).show();

        // SUBIDA EN HILO SEPARADO
        new Thread(() -> {

            List<String> finalImageUrls = new ArrayList<>();
            CloudinaryUploader uploader = new CloudinaryUploader();

            for (String localUri : localPhotoUris) {
                Uri uri = Uri.parse(localUri);

                String uploadedUrl = uploader.uploadImage(requireContext(), uri);

                if (uploadedUrl == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error subiendo imagen", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                finalImageUrls.add(uploadedUrl);
            }

            // Crear producto
            Product product = new Product(
                    productId,
                    title,
                    selectedCategory,
                    price,
                    description,
                    user.getUid(),
                    timestamp,
                    extra
            );

            product.setImageUrls(finalImageUrls);

            new ProductRepository().uploadProduct(product)
                    .addOnSuccessListener(v ->
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Producto publicado!", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                    )
                    .addOnFailureListener(e ->
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(),
                                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            )
                    );

        }).start();
    }
}
