package com.tzolas.camisetaswallapop.models;

public class Message {

    private String id;
    private String senderId;
    private String text;
    private long timestamp;
    private boolean delivered;
    private boolean read;

    private String type;       // "text", "offer", "system", etc.
    private int offerPrice;    // puntos ofertados
    private String status;     // "pending", "accepted", "rejected"

    public Message() {}

    public Message(String id, String senderId, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.delivered = false;
        this.read = false;
        this.type = "text";
        this.status = "pending";
    }

    // getters & setters...

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getOfferPrice() { return offerPrice; }
    public void setOfferPrice(int offerPrice) { this.offerPrice = offerPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
