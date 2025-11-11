package com.tzolas.camisetaswallapop.models;

public class ChatRequest {
    public String id;
    public String name;
    public String avatarUrl;

    public ChatRequest(String id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
}

