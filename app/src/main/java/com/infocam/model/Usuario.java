package com.infocam.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

// Modelo que representa al Usuario. Coincide con la respuesta del endpoint.

public class Usuario {
    private Integer id;

    @SerializedName(value = "is_admin", alternate = { "admin" })
    private boolean isAdmin; // Desde la aplicación móvil, ningún usuario que se registre podrá ser nunca
                             // "Admin", por lo que este campo siempre será false.

    private String username;
    private String password;

    @SerializedName(value = "nombre", alternate = { "firstName" })
    private String nombre;

    @SerializedName(value = "apellido", alternate = { "lastName" })
    private String apellido;

    private String email;
    private long telefono;
    private List<Incidencia> incidencias = new ArrayList<>(); // Comienza vacío.
    private List<Camara> favoritos = new ArrayList<>(); // Comienza vacío.
    private String token; // Token de inicio de sesión. Cuando te logueas por primera vez, se te da este
                          // "pase" para poder identificarte sin tener que añadir la contraseña otra vez.

    public Usuario() {
    }

    public Usuario(Integer id, boolean isAdmin, String username, String password, String nombre, String apellido,
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public List<Incidencia> getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(List<Incidencia> incidencias) {
        this.incidencias = incidencias;
    }

    public List<Camara> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(List<Camara> favoritos) {
        this.favoritos = favoritos;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
