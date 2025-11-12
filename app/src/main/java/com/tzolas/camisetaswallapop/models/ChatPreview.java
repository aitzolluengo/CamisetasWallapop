package com.tzolas.camisetaswallapop.models;

public class ChatPreview {
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserPhoto;
    private String lastMessage;
    private long lastMessageTime;

    public ChatPreview() {}

    public ChatPreview(String chatId, String otherUserId, String otherUserName, String otherUserPhoto, String lastMessage, long lastMessageTime) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserPhoto = otherUserPhoto;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getChatId() { return chatId; }
    public String getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public String getOtherUserPhoto() { return otherUserPhoto; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTime() { return lastMessageTime; }

    public void setChatId(String chatId) { this.chatId = chatId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public void setOtherUserPhoto(String otherUserPhoto) { this.otherUserPhoto = otherUserPhoto; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
