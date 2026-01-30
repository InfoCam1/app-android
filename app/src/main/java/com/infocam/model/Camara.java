package com.infocam.model;

// Modelo para las cámaras de tráfico. Se mostrarán con un marcador verde en el mapa.
public class Camara {
    private int id;
    private String nombre;
    private double latitud;
    private double longitud;
    private String imagen; // URL de la imagen de la cámara
    private boolean activa;

    // Siempre crearemos un par de constructores, uno vacío y otro con los datos del modelo, para poder llamarlo en caso de necesitarlo.
    public Camara() {
    }

    public Camara(int id, String nombre, double latitud, double longitud, String imagen, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.imagen = imagen;
        this.activa = activa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
