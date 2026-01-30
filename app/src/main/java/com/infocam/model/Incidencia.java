package com.infocam.model;

import com.google.gson.annotations.SerializedName;

// Modelo para las incidencias reportadas por usuarios. Estas podrán ser propias o del Gobierno Vasco (estas tendrán en el servidor un "externalId"). Se mostrarán con un marcados naranja, en caso de las propias, o rojo.
public class Incidencia {
    private Integer id;
    private int idUsuario; // ID del usuario que creó la incidencia

    @SerializedName("usuario")
    private UsuarioNested usuario;

    private String externalId; // ID externa para incidencias de OpenData
    private String nombre;
    private String tipoIncidencia;
    private String causa;

    private static class UsuarioNested {
        int id;
    }

    @SerializedName(value = "fecha_inicio", alternate = { "fechaInicio" })
    private String fechaInicio;

    @SerializedName(value = "fecha_fin", alternate = { "fechaFin" })
    private String fechaFin;

    private double latitud;
    private double longitud;

    public Incidencia() {
    }

    public Incidencia(int idUsuario, String nombre, String tipoIncidencia, String causa, String fechaInicio,
            String fechaFin, double latitud, double longitud, String externalId) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.tipoIncidencia = tipoIncidencia;
        this.causa = causa;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.latitud = latitud;
        this.longitud = longitud;
        this.externalId = externalId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdUsuario() {
        if (idUsuario == 0 && usuario != null) {
            return usuario.id;
        }
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        // La API de Spring Boot prefiere recibir la relación como un objeto anidado
        // "usuario": {"id": ...}
        if (this.usuario == null) {
            this.usuario = new UsuarioNested();
        }
        this.usuario.id = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(String tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public String getCausa() {
        return causa;
    }

    public void setCausa(String causa) {
        this.causa = causa;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Helper para saber si la incidencia es oficial (OpenData)
     */
    public boolean isOficial() {
        return externalId != null && !externalId.isEmpty() && !externalId.equals("null");
    }
}
