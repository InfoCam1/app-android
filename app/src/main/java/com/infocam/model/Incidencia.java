package com.infocam.model;

/**
 * Modelo para las incidencias reportadas por usuarios.
 * Se mostrarán como marcadores ROJOS en el mapa.
 */
public class Incidencia {
    private int id;
    private int idUsuario; // ID del usuario que creó la incidencia
    private String externalId; // ID externa para incidencias de OpenData
    private String nombre;
    private String tipoIncidencia;
    private String causa;
    private String fechaInicio;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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
