package com.infocam.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infocam.R;
import com.infocam.data.DataRepository;
import com.infocam.data.SessionManager;
import com.infocam.model.Camara;
import com.infocam.model.Usuario;
import com.infocam.network.ApiCallback;
import com.infocam.network.InfocamServiceClient;

import java.util.List;

/**
 * LoginActivity: Pantalla de acceso que actúa como "Guard" de la aplicación.
 * 
 * Conceptos clave para DAM:
 * 1. Intent: Objeto que permite la navegación entre pantallas (Activities).
 * 2. Validación de entrada: Comprobar que los campos no estén vacíos antes de
 * lanzar peticiones.
 * 3. Asincronía: Las peticiones de red NO pueden ir en el hilo principal (UI
 * Thread).
 */
public class LoginActivity extends AppCompatActivity {

    private EditText campoUsuario;
    private EditText campoContrasena;
    private Button btnAcceder;
    private SessionManager preferenciaSesion;
    private DataRepository baseDatosLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenciaSesion = new SessionManager(this);
        baseDatosLocal = new DataRepository(this);

        // Control de flujo: Si el usuario ya está logueado, saltamos al mapa
        if (preferenciaSesion.estaLogueado()) {
            navegarAMapaPrincipal();
            return;
        }

        campoUsuario = findViewById(R.id.etUsername);
        campoContrasena = findViewById(R.id.etPassword);
        btnAcceder = findViewById(R.id.btnLogin);

        // Evento de Login
        btnAcceder.setOnClickListener(v -> validarYEnviarDatos());

        // Navegación a Registro
        findViewById(R.id.tvGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /**
     * Realiza validaciones básicas antes de conectar con el API Rest.
     */
    private void validarYEnviarDatos() {
        String u = campoUsuario.getText().toString().trim();
        String p = campoContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Introduce usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAcceder.setEnabled(false); // Feedback visual y prevención de doble click

        // Petición al API mediante el Singleton de InfocamServiceClient
        InfocamServiceClient.obtenerInstancia().iniciarSesion(u, p, new ApiCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario user) {
                // Guardamos la sesión persistente en SharedPreferences
                preferenciaSesion.guardarSesion(user);

                // Intentamos traer sus favoritos para que aparezcan en el mapa nada más entrar
                descargarYEntrar(user);
            }

            @Override
            public void onError(String error) {
                btnAcceder.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void descargarYEntrar(Usuario user) {
        InfocamServiceClient.obtenerInstancia().obtenerFavoritosUsuario(user.getToken(), user.getId(),
                new ApiCallback<List<Camara>>() {
                    @Override
                    public void onSuccess(List<Camara> resultado) {
                        baseDatosLocal.sincronizarConServidor(user.getId(), resultado);
                        navegarAMapaPrincipal();
                    }

                    @Override
                    public void onError(String error) {
                        // Si la sincronización inicial falla por red, entramos igualmente (Offline
                        // first)
                        navegarAMapaPrincipal();
                    }
                });
    }

    private void navegarAMapaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Cerramos el Login para que no se pueda volver atrás con el botón físico
    }
}
