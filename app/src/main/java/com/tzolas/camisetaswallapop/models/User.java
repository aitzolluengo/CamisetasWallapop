package com.tzolas.camisetaswallapop.models;

public class User {
    private String uid;
    private String name;     // nombre visible
    private String email;
    private String photo;    // url foto
    private Long createdAt;  // opcional para lecturas simples

    public User() {}

    public User(String uid, String name, String email, String photo, Long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.createdAt = createdAt;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
