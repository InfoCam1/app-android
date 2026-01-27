package com.infocam.model;

/**
 * Modelo que representa al Usuario autenticado.
 * Coincide con la respuesta del endpoint /login.
 */
public class Usuario {
    private int id;
    private boolean isAdmin; // "is_admin": false
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
    private long telefono; // puede ser 0
    private String[] incidencias; // array vacío en screenshot, asumo String[] o Object[] por ahora, o List
    private String[] favoritos; // array vacío
    private String token; // Token JWT

    // Constructor vacío
    public Usuario() {
    }

    public Usuario(int id, boolean isAdmin, String username, String password, String nombre, String apellido,
            String email, long telefono, String token) {
        this.id = id;
        this.isAdmin = isAdmin;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTelefono() {
        return telefono;
    }

    public void setTelefono(long telefono) {
        this.telefono = telefono;
    }

    public String[] getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(String[] incidencias) {
        this.incidencias = incidencias;
    }

    public String[] getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(String[] favoritos) {
        this.favoritos = favoritos;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
