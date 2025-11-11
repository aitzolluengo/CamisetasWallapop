package com.tzolas.camisetaswallapop.models;

public class Chat {

    private String id;
    private String user1;
    private String user2;
    private String productId;
    private long createdAt;

    public Chat() {}

    public Chat(String id, String user1, String user2, String productId, long createdAt) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUser1() { return user1; }
    public String getUser2() { return user2; }
    public String getProductId() { return productId; }
    public long getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUser1(String user1) { this.user1 = user1; }
    public void setUser2(String user2) { this.user2 = user2; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
