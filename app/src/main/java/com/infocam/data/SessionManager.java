package com.infocam.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.infocam.model.Usuario;

/**
 * SessionManager: Gestiona la persistencia de la sesión mediante
 * SharedPreferences.
 * 
 * Conceptos clave para DAM:
 * 1. SharedPreferences: Almacenamiento clave-valor ideal para datos de
 * configuración o sesión.
 * 2. apply() vs commit(): apply() es asíncrono (no bloquea UI), commit() es
 * síncrono.
 * 3. Encapsulamiento: Centralizamos el acceso a las preferencias para que el
 * resto de la app
 * no conozca las claves (keys).
 */
public class SessionManager {

    private static final String NOMBRE_PREFERENCIAS = "InfoCamSession";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_ID_USUARIO = "user_id";
    private static final String CLAVE_ESTA_LOGUEADO = "is_logged_in";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Almacena los datos del usuario tras un inicio de sesión correcto.
     */
    public void guardarSesion(Usuario usuario) {
        if (usuario == null)
            return;

        editor.putBoolean(CLAVE_ESTA_LOGUEADO, true);
        editor.putString(CLAVE_TOKEN, usuario.getToken());
        editor.putInt(CLAVE_ID_USUARIO, usuario.getId());

        // Guardamos también otros campos para mostrarlos en el perfil
        editor.putString("username", usuario.getUsername());
        editor.putString("nombre", usuario.getNombre());
        editor.putString("email", usuario.getEmail());
        editor.putLong("telefono", usuario.getTelefono());

        editor.apply();
    }

    /**
     * Construye un objeto Usuario a partir de los datos guardados en disco.
     */
    public Usuario obtenerUsuario() {
        if (!estaLogueado())
            return null;

        Usuario user = new Usuario();
        user.setId(pref.getInt(CLAVE_ID_USUARIO, -1));
        user.setUsername(pref.getString("username", ""));
        user.setNombre(pref.getString("nombre", "Usuario"));
        user.setEmail(pref.getString("email", ""));
        user.setTelefono(pref.getLong("telefono", 0));
        user.setToken(pref.getString(CLAVE_TOKEN, null));
        return user;
    }

    /**
     * Devuelve el token JWT necesario para todas las peticiones al API.
     */
    public String getToken() {
        return pref.getString(CLAVE_TOKEN, null);
    }

    public boolean estaLogueado() {
        return pref.getBoolean(CLAVE_ESTA_LOGUEADO, false);
    }

    /**
     * Borra todos los datos de la sesión (Cierre de seguridad).
     */
    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}
