package com.tzolas.camisetaswallapop.models;

public class Message {

    private String id;
    private String senderId;
    private String text;
    private long timestamp;
    private boolean delivered;
    private boolean read;


    public Message() {}

    public Message(String id, String senderId, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.delivered = false;
        this.read = false;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isDelivered() { return delivered; }
    public boolean isRead() { return read; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    public void setRead(boolean read) { this.read = read; }
}
