package com.infocam.model;

import com.google.gson.annotations.SerializedName;

// Este modelo tratará las peticiones de inicio de sesión. Solo contiene los campos necesarios, cogidos de Usuario, pudiendo crear así un objeto LoginRequest y evitando errores de formato.
public class LoginRequest {
    @SerializedName("username")
    private String nombreUsuario;
    @SerializedName("password")
    private String contrasena;

    public LoginRequest(String nombreUsuario, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }
}
