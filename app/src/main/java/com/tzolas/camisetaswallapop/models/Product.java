package com.tzolas.camisetaswallapop.models;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Keep
public class Product {

    private String id;
    private String title;
    private String category;
    private double price;
    private String description;

    // 🔥 Soporta múltiples fotos
    private List<String> imageUrls = new ArrayList<>();

    private String userId;
    private long timestamp;
    private Map<String, Object> extra;

    // Campos de venta
    private boolean sold;
    private String buyerId;
    private String orderId;
    private long soldAt;

    private boolean favourite;

    private String shippingAddress;
    private String postalCode;
    private String phone;

    // 🔴 Firestore exige constructor vacío
    public Product() {}

    // Constructor principal
    public Product(String id, String title, String category, double price,
                   String description, String userId, long timestamp,
                   Map<String, Object> extra) {

        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.description = description;
        this.userId = userId;
        this.timestamp = timestamp;
        this.extra = extra;

        this.sold = false;
        this.buyerId = null;
        this.orderId = null;
        this.soldAt = 0L;
        this.favourite = false;
        this.imageUrls = new ArrayList<>();
    }

    // ------------------------------------
    //           GETTERS / SETTERS
    // ------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImageUrls() { return imageUrls; }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls == null) ? new ArrayList<>() : imageUrls;
    }

    // 🔥 FIX: Firestore enviaba "imageUrl"
    // Este setter convierte imageUrl -> imageUrls automáticamente
    public void setImageUrl(String url) {
        if (url != null && !url.isEmpty()) {
            this.imageUrls = Arrays.asList(url);
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public long getSoldAt() { return soldAt; }
    public void setSoldAt(long soldAt) { this.soldAt = soldAt; }

    public boolean isFavorite() { return favourite; }
    public void setFavorite(boolean favourite) { this.favourite = favourite; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
