package com.infocam.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

// Esta clase gestiona el acceso de usuarios y la persistencia de sus datos de sesión.
public class LoginActivity extends AppCompatActivity {

    private EditText campoUsuario;
    private EditText campoContrasena;
    private Button btnAcceder;
    // Estos son los gestores de datos (locales).
    private SessionManager preferenciaSesion; // Gestiona la sesión del usuario.
    private DataRepository baseDatosLocal; // Gestiona la base de datos local.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Iniciamos los gestores con el contexto de la actividad.
        preferenciaSesion = new SessionManager(this);
        baseDatosLocal = new DataRepository(this);

        // Si el usuario ya está logueado, saltamos al mapa
        if (preferenciaSesion.estaLogueado()) {
            navegarAMapaPrincipal();
            return;
        }

        campoUsuario = findViewById(R.id.etUsername);
        campoContrasena = findViewById(R.id.etPassword);
        btnAcceder = findViewById(R.id.btnLogin);

        // Evento de Login.
        btnAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYEnviarDatos();
            }
        });

        // Navegación hasta la pantalla de registro.
        findViewById(R.id.tvGoToRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); // Se inicia la actividad de
                                                                                       // registro.
            }
        });
    }

    // Realizamos validaciones básicas antes de conectar con el API Rest.
    private void validarYEnviarDatos() {
        String u = campoUsuario.getText().toString().trim();
        String p = campoContrasena.getText().toString().trim();

        // Evitamos que haya campos vacíos.
        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Introduce usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAcceder.setEnabled(false); // Bloqueamos el botón para evitar varias peticiones seguidas.

        // Realizamos la petición al API mediante InfocamServiceClient. La llamada es
        // asíncrona, no bloquea el hilo principal y la aplicación sigue respondiendo.
        InfocamServiceClient.obtenerInstancia().iniciarSesion(u, p, new ApiCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario user) {
                // Si la petición es exitosa, guardamos la sesión en "SharedPreferences".
                preferenciaSesion.guardarSesion(user);
                // Intentamos traer sus favoritos para que aparezcan en el mapa nada más entrar.
                descargarYEntrar(user);
            }

            @Override
            public void onError(String error) {
                // Si la petición falla, reactivamos el botón y mostramos el error.
                btnAcceder.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Descargamos los favoritos del usuario del servidor.
    private void descargarYEntrar(Usuario user) {
        InfocamServiceClient.obtenerInstancia().obtenerFavoritosUsuario(user.getToken(), user.getId(),
                new ApiCallback<List<Camara>>() {
                    @Override
                    public void onSuccess(List<Camara> resultado) {
                        // Guardamos los datos remotos en la base de datos local.
                        baseDatosLocal.sincronizarConServidor(user.getId(), resultado);
                        navegarAMapaPrincipal();
                    }

                    @Override
                    public void onError(String error) {
                        // Si la sincronización inicial falla por red, entramos igualmente.
                        navegarAMapaPrincipal();
                    }
                });
    }

    private void navegarAMapaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Eliminamos esta actividad del historial de actividades para que, al pulsar
                  // "atrás", el usuario no regrese al login.
    }
}
