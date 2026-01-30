package com.infocam.model;

// Este modelo tratará las peticiones de inicio de sesión. Solo contiene los campos necesarios, cogidos de Usuario, pudiendo crear así un objeto LoginRequest y evitando errores de formato.
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
