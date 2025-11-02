package com.tzolas.camisetaswallapop.models;

public class Camiseta {

    private String id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private double precio;
    private String userId;

    // 🔹 Constructor vacío (obligatorio para Firestore)
    public Camiseta() {}

    // 🔹 Constructor completo (para Firestore)
    public Camiseta(String id, String nombre, String descripcion, String imagenUrl, double precio, String userId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.precio = precio;
        this.userId = userId;
    }

    // 🔹 Constructor corto (para tests o datos de ejemplo locales)
    public Camiseta(String nombre, String descripcion, double precio, int imagenResId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = String.valueOf(imagenResId);
    }

    // 🔹 Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
