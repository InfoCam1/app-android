package com.infocam.model;

// Modelo para guardar favoritos. Lo utilizaremos principalmente en la base de datos local de SQLite. Se mostrarán con un marcador amarillo en el mapa.
public class Favorito {
    private int idLocal;
    private int idUsuario;
    private int idCamara;
    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private String imagen; // URL de la imagen de la cámara

    public Favorito() {
    }

    public Favorito(int idUsuario, int idCamara, String nombre, String direccion, double latitud, double longitud,
            String imagen) {
        this.idUsuario = idUsuario;
        this.idCamara = idCamara;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.imagen = imagen;
    }

    public int getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(int idLocal) {
        this.idLocal = idLocal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdCamara() {
        return idCamara;
    }

    public void setIdCamara(int idCamara) {
        this.idCamara = idCamara;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public String getUrlImagen() {
        return imagen;
    }

    public void setUrlImagen(String imagen) {
        this.imagen = imagen;
    }
}
