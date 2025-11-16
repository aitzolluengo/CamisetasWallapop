package com.tzolas.camisetaswallapop.models;

public class ShippingInfo {
    private String address;
    private String postalCode;
    private String phone;
    private String buyerId;
    private String productId;

    public ShippingInfo() {}

    public ShippingInfo(String address, String postalCode, String phone, String buyerId, String productId) {
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.buyerId = buyerId;
        this.productId = productId;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
