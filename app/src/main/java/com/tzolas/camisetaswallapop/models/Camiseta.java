package com.tzolas.camisetaswallapop.models;

public class Camiseta {
    private String equipo;
    private String talla;
    private double precio;
    private int imagen; // por ahora usamos un drawable local

    public Camiseta(String equipo, String talla, double precio, int imagen) {
        this.equipo = equipo;
        this.talla = talla;
        this.precio = precio;
        this.imagen = imagen;
    }

    public String getEquipo() { return equipo; }
    public String getTalla() { return talla; }
    public double getPrecio() { return precio; }
    public int getImagen() { return imagen; }
}
