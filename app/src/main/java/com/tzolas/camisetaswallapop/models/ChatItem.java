package com.tzolas.camisetaswallapop.models;

public class ChatItem {
    public String id;
    public String name;
    public String lastMessage;
    public String avatarUrl;

    public ChatItem(String id, String name, String lastMessage, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.avatarUrl = avatarUrl;
    }
}
