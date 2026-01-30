package com.infocam.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.infocam.model.Usuario;

/* Con "SharedPreferences", en esta clase guardaremos el estado de la sesión (si hay alguien con ella iniciada) y los datos del usuario
 * logueado entre otros.
 * Los conceptos clave de esta clase son:
 * 1. SharedPreferences: almacenará los datos de configuración/sesión. Aunque cierres la aplicación, la sesión se quedará guardada.
 * 2. Encapsulamiento: centralizamos el acceso a las preferencias, haremos que el resto de la aplicación no conozca las claves. */
public class SessionManager {
    private static final String NOMBRE_PREFERENCIAS = "InfoCamSession"; // Guardaremos todo en un archivo XML con este nombre.
    // Meteremos las llaves en variables con nombres fáciles de recordar.
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_ID_USUARIO = "user_id";
    private static final String CLAVE_ESTA_LOGUEADO = "is_logged_in";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    // Creamos el constructor que pasaremos a "Context" para que pueda acceder a los archivos de la aplicación.
    public SessionManager(Context context) {
        pref = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        editor = pref.edit(); // Necesitaremos también el editor para poder escribir o borrar dichas preferencias.
    }

    /* Creamos el método que guardará los datos del usuario. Este se lanzará cuando la API confirme que el login es correcto.
     * En ese momento, guardaremos todos los datos para poder disponer de ellos posteriormente. */
    public void guardarSesion(Usuario usuario) {
        // Si no hay ningún usuario logueado sale del método.
        if (usuario == null)
            return;

        editor.putBoolean(CLAVE_ESTA_LOGUEADO, true);
        editor.putString(CLAVE_TOKEN, usuario.getToken());
        editor.putInt(CLAVE_ID_USUARIO, usuario.getId());

        // Guardamos también otros campos para mostrarlos en el perfil.
        editor.putString("username", usuario.getUsername());
        editor.putString("nombre", usuario.getNombre());
        editor.putString("email", usuario.getEmail());
        editor.putLong("telefono", usuario.getTelefono());

        editor.apply(); // El "apply()" se realizará en segundo plano, así el usuario no lo notará.
    }

    // Cuando necesiteos mostrar los datos del usuario en la interfaz del perfil, el método rescatará los datos guardados y creará un nuevo objeto Usuario.
    public Usuario obtenerUsuario() {
        // Si no hay ningún usuario logueado sale del método.
        if (!estaLogueado())
            return null;

        Usuario user = new Usuario();
        // Añadiremos también valores por defecto, para rellenar algún hueco si falta esa información.
        user.setId(pref.getInt(CLAVE_ID_USUARIO, -1));
        user.setUsername(pref.getString("username", "usuario01"));
        user.setNombre(pref.getString("nombre", "Usuario"));
        user.setEmail(pref.getString("email", "email@email.com"));
        user.setTelefono(pref.getLong("telefono", 943000000));
        user.setToken(pref.getString(CLAVE_TOKEN, null));
        return user;
    }

    // El token es como un pasaporte que usaremos para comunicarnos con el servidor. crearemos un Getter para que nos sea más fácil utilizarlo.
    public String getToken() {
        return pref.getString(CLAVE_TOKEN, null);
    }

    // También creremos un método que facilite saber si existe una sesión activa o no.
    public boolean estaLogueado() {
        return pref.getBoolean(CLAVE_ESTA_LOGUEADO, false);
    }

    // Cuando cerremos sesión, los datos del XML se borrarán por completo por seguridad.
    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}
