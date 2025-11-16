package com.tzolas.camisetaswallapop.models;

public class User {
    private String uid;
    private String name;     // nombre visible
    private String email;
    private String photo;    // url foto
    private Long createdAt;  // opcional para lecturas simples
    private double ratingSum;
    private long ratingCount;
    private int points;      // puntos disponibles
    private int spentPoints; // opcional, estadísticas


    public User() {}

    public User(String uid, String name, String email, String photo, Long createdAt, double pRatingSum, long pRatingCount) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.createdAt = createdAt;
        this.ratingCount=pRatingCount;
        this.ratingSum=pRatingCount;
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
    public double getRatingSum() { return ratingSum; }
    public void setRatingSum(double ratingSum) { this.ratingSum = ratingSum; }

    public long getRatingCount() { return ratingCount; }
    public void setRatingCount(long ratingCount) { this.ratingCount = ratingCount; }

    public double getAvgRating() {
        return ratingCount > 0 ? ratingSum / ratingCount : 0.0;
    }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getSpentPoints() { return spentPoints; }
    public void setSpentPoints(int spentPoints) { this.spentPoints = spentPoints; }

}
