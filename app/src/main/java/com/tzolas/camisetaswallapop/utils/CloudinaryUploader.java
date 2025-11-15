package com.tzolas.camisetaswallapop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudinaryUploader {

    private static final String CLOUD_NAME = "drpiboyfe";
    private static final String UPLOAD_PRESET = "unsigned";

    private static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    public String uploadImage(Context context, Uri uri) {

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // 👇 Decodifica como Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            // 👇 COMPRESIÓN 85%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);

            byte[] imageBytes = baos.toByteArray();

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            "image.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/jpeg"))
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String raw = response.body().string();

            System.out.println("🌐 CLOUDINARY → " + raw);

            if (!response.isSuccessful()) return null;

            JSONObject json = new JSONObject(raw);
            return json.getString("secure_url");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
