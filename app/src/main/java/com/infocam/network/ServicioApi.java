package com.infocam.network;

import android.os.Handler;
import android.os.Looper;
import com.infocam.model.Camara;
import com.infocam.model.Incidencia;
import com.infocam.model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServicioApi: Esta clase es el "Motor de Red" de nuestra aplicación.
 * 
 * Aplicamos el patrón SINGLETON para garantizar que solo exista una instancia
 * de comunicación en toda la app, ahorrando recursos y centralizando la
 * configuración.
 * 
 * Conceptos clave para DAM:
 * 1. Hilos (Threads): Las peticiones de red NO pueden ir en el hilo principal
 * (UI Thread).
 * 2. Handlers: Usamos un Handler para enviar los resultados de vuelta al hilo
 * principal y actualizar la vista.
 * 3. JSON: Parseo manual para demostrar conocimiento del formato de intercambio
 * de datos.
 */
public class ServicioApi {

    // URL base de nuestro servidor (cambiar según el entorno)
    private static final String URL_BASE = "http://10.10.16.85:8080/api";

    private static ServicioApi instanciaUnica;

    // El "Ejecutor" gestiona una cola de hilos para no bloquear la interfaz de
    // usuario
    private final ExecutorService ejecutorHilos;

    // El "Manejador" nos permite comunicarnos con el hilo de la interfaz de usuario
    private final Handler manejadorUI;

    // Constructor privado para el patrón Singleton
    private ServicioApi() {
        // Creamos un pool de 4 hilos para procesar hasta 4 peticiones simultáneas
        ejecutorHilos = Executors.newFixedThreadPool(4);
        manejadorUI = new Handler(Looper.getMainLooper());
    }

    /**
     * Devuelve la instancia única de la clase (Singleton).
     */
    public static synchronized ServicioApi obtenerInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new ServicioApi();
        }
        return instanciaUnica;
    }

    // ================================================================================
    // GESTIÓN DE USUARIOS
    // ================================================================================

    /**
     * Envía las credenciales al servidor para obtener un token de acceso.
     */
    public void iniciarSesion(String nombreUsuario, String contrasena, ApiCallback<Usuario> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/auth/login").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json");
                conexion.setDoOutput(true); // Permitimos enviar datos en el cuerpo (body)

                // Creamos el JSON con las credenciales
                JSONObject datos = new JSONObject();
                datos.put("username", nombreUsuario);
                datos.put("password", contrasena);

                // Enviamos los datos al flujo de salida
                try (OutputStream os = conexion.getOutputStream()) {
                    byte[] entrada = datos.toString().getBytes("utf-8");
                    os.write(entrada, 0, entrada.length);
                }

                int codigoEstado = conexion.getResponseCode();
                if (codigoEstado >= 200 && codigoEstado < 300) {
                    // Si el login es correcto, el servidor puede enviar el Token en la cabecera o
                    // en el JSON
                    String token = conexion.getHeaderField("Authorization");
                    String respuestaContenido = leerFlujoEntrada(conexion);
                    JSONObject jsonRespuesta = new JSONObject(respuestaContenido);

                    if (token == null && jsonRespuesta.has("token")) {
                        token = jsonRespuesta.optString("token");
                    }

                    // Limpiamos el prefijo 'Bearer ' si existe
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }

                    // Convertimos el JSON en un objeto Usuario (POJO)
                    Usuario usuario = parsearUsuario(jsonRespuesta);
                    if (usuario.getToken() == null) {
                        usuario.setToken(token);
                    }

                    enviarResultadoAlHiloPrincipal(callback, usuario);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Credenciales incorrectas (Código: " + codigoEstado + ")");
                }

            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, "Error de conexión: " + e.getMessage());
            }
        });
    }

    /**
     * Registra un nuevo usuario en la base de datos remota.
     */
    public void registrarUsuario(Usuario usuario, ApiCallback<Void> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/auth/registro").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json");
                conexion.setDoOutput(true);

                JSONObject datos = new JSONObject();
                datos.put("username", usuario.getUsername());
                datos.put("password", usuario.getPassword());
                datos.put("email", usuario.getEmail());
                datos.put("nombre", usuario.getNombre());
                datos.put("apellido", usuario.getApellido());
                datos.put("telefono", usuario.getTelefono());
                datos.put("is_admin", false); // Por defecto registro de usuario normal

                try (OutputStream os = conexion.getOutputStream()) {
                    byte[] buffer = datos.toString().getBytes("utf-8");
                    os.write(buffer, 0, buffer.length);
                }

                if (conexion.getResponseCode() >= 200 && conexion.getResponseCode() < 300) {
                    enviarResultadoAlHiloPrincipal(callback, null);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "No se pudo completar el registro.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    /**
     * Actualiza los datos del perfil del usuario.
     */
    public void actualizarUsuario(String token, int id, String nombre, String email, long telefono, String password,
            ApiCallback<Usuario> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/usuarios/" + id).toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("PUT");
                conexion.setRequestProperty("Content-Type", "application/json");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);
                conexion.setDoOutput(true);

                JSONObject datos = new JSONObject();
                datos.put("nombre", nombre);
                datos.put("email", email);
                datos.put("telefono", telefono);
                if (password != null && !password.isEmpty()) {
                    datos.put("password", password);
                }

                try (OutputStream os = conexion.getOutputStream()) {
                    byte[] buffer = datos.toString().getBytes("utf-8");
                    os.write(buffer, 0, buffer.length);
                }

                if (conexion.getResponseCode() >= 200 && conexion.getResponseCode() < 300) {
                    String contenido = leerFlujoEntrada(conexion);
                    Usuario actualizado = parsearUsuario(new JSONObject(contenido));
                    enviarResultadoAlHiloPrincipal(callback, actualizado);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al actualizar los datos en el servidor.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    // ================================================================================
    // GESTIÓN DE CÁMARAS Y FAVORITOS
    // ================================================================================

    public void obtenerCamarasActivas(String token, ApiCallback<List<Camara>> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/camaras/activas").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("GET");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);

                if (conexion.getResponseCode() == 200) {
                    String contenido = leerFlujoEntrada(conexion);
                    JSONArray jsonArray = new JSONArray(contenido);
                    List<Camara> lista = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        lista.add(parsearCamara(jsonArray.getJSONObject(i)));
                    }
                    enviarResultadoAlHiloPrincipal(callback, lista);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al descargar cámaras.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    public void conmutarFavorito(String token, int idCamara, int idUsuario, ApiCallback<Void> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/camaras/" + idCamara + "/favorita?usuarioId=" + idUsuario)
                        .toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);

                if (conexion.getResponseCode() >= 200 && conexion.getResponseCode() < 300) {
                    enviarResultadoAlHiloPrincipal(callback, null);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "No se pudo cambiar el estado de favorito.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    public void obtenerFavoritosUsuario(String token, int idUsuario, ApiCallback<List<Camara>> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/usuarios/" + idUsuario + "/favoritos/camaras").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("GET");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);

                if (conexion.getResponseCode() == 200) {
                    String respuesta = leerFlujoEntrada(conexion);
                    JSONArray array = new JSONArray(respuesta);
                    List<Camara> lista = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        lista.add(parsearCamara(array.getJSONObject(i)));
                    }
                    enviarResultadoAlHiloPrincipal(callback, lista);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al sincronizar favoritos.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    public void obtenerTiposIncidencia(String token, ApiCallback<List<String>> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/incidencias/tipos").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("GET");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);

                if (conexion.getResponseCode() == 200) {
                    String contenido = leerFlujoEntrada(conexion);
                    JSONArray array = new JSONArray(contenido);
                    List<String> lista = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        lista.add(array.getString(i));
                    }
                    enviarResultadoAlHiloPrincipal(callback, lista);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al obtener tipos de incidencia.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    public void obtenerIncidencias(String token, Integer idUsuario, ApiCallback<List<Incidencia>> callback) {
        ejecutorHilos.execute(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                String fechaActual = sdf.format(new Date());

                String urlString = URL_BASE + "/incidencias/activas?fecha=" + fechaActual;
                if (idUsuario != null) {
                    urlString += "&usuarioId=" + idUsuario;
                }

                URL url = URI.create(urlString).toURL();
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);

                if (conexion.getResponseCode() == 200) {
                    String texto = leerFlujoEntrada(conexion);
                    JSONArray jsonArray = new JSONArray(texto);
                    List<Incidencia> lista = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        lista.add(parsearIncidencia(jsonArray.getJSONObject(i)));
                    }
                    enviarResultadoAlHiloPrincipal(callback, lista);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al obtener incidencias.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    public void crearIncidencia(String token, Incidencia incidencia, ApiCallback<Void> callback) {
        ejecutorHilos.execute(() -> {
            try {
                URL direccion = URI.create(URL_BASE + "/incidencias").toURL();
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json");
                if (token != null)
                    conexion.setRequestProperty("Authorization", "Bearer " + token);
                conexion.setDoOutput(true);

                JSONObject json = new JSONObject();
                JSONObject jsonUser = new JSONObject();
                jsonUser.put("id", incidencia.getIdUsuario());
                json.put("usuario", jsonUser);

                json.put("nombre", incidencia.getNombre());
                json.put("tipoIncidencia", incidencia.getTipoIncidencia());
                json.put("causa", incidencia.getCausa());
                json.put("latitud", incidencia.getLatitud());
                json.put("longitud", incidencia.getLongitud());
                json.put("fecha_inicio", incidencia.getFechaInicio());
                json.put("fecha_fin", incidencia.getFechaFin());

                try (OutputStream os = conexion.getOutputStream()) {
                    os.write(json.toString().getBytes("utf-8"));
                }

                if (conexion.getResponseCode() >= 200 && conexion.getResponseCode() < 300) {
                    enviarResultadoAlHiloPrincipal(callback, null);
                } else {
                    enviarErrorAlHiloPrincipal(callback, "Error al reportar la incidencia.");
                }
            } catch (Exception e) {
                enviarErrorAlHiloPrincipal(callback, e.getMessage());
            }
        });
    }

    private Usuario parsearUsuario(JSONObject obj) {
        Usuario u = new Usuario();
        u.setId(obj.optInt("id"));
        u.setAdmin(obj.optBoolean("is_admin", obj.optBoolean("admin", false)));
        u.setUsername(obj.optString("username"));
        u.setNombre(obj.optString("nombre", obj.optString("firstName", "")));
        u.setApellido(obj.optString("apellido", obj.optString("lastName", "")));
        u.setEmail(obj.optString("email"));
        u.setTelefono(obj.optLong("telefono", 0));
        return u;
    }

    private Camara parsearCamara(JSONObject obj) {
        Camara c = new Camara();
        c.setId(obj.optInt("id"));
        c.setNombre(obj.optString("nombre"));
        c.setLatitud(obj.optDouble("latitud"));
        c.setLongitud(obj.optDouble("longitud"));
        c.setImagen(obj.optString("imagen"));
        c.setActiva(obj.optBoolean("activa"));
        return c;
    }

    private Incidencia parsearIncidencia(JSONObject obj) {
        Incidencia inc = new Incidencia();
        inc.setId(obj.optInt("id"));

        if (obj.has("idUsuario")) {
            inc.setIdUsuario(obj.optInt("idUsuario"));
        } else if (obj.has("usuario")) {
            JSONObject uObj = obj.optJSONObject("usuario");
            if (uObj != null)
                inc.setIdUsuario(uObj.optInt("id"));
        }

        inc.setNombre(obj.optString("nombre"));
        inc.setTipoIncidencia(obj.optString("tipoIncidencia"));
        inc.setCausa(obj.optString("causa"));
        inc.setFechaInicio(obj.optString("fecha_inicio", obj.optString("fechaInicio")));
        inc.setFechaFin(obj.optString("fecha_fin", obj.optString("fechaFin")));
        inc.setLatitud(obj.optDouble("latitud"));
        inc.setLongitud(obj.optDouble("longitud"));
        inc.setExternalId(obj.optString("externalId", null));
        return inc;
    }

    private String leerFlujoEntrada(HttpURLConnection conexion) throws Exception {
        BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
        StringBuilder resultado = new StringBuilder();
        String linea;
        while ((linea = lector.readLine()) != null) {
            resultado.append(linea);
        }
        lector.close();
        return resultado.toString();
    }

    private <T> void enviarResultadoAlHiloPrincipal(ApiCallback<T> callback, T resultado) {
        manejadorUI.post(() -> {
            if (callback != null)
                callback.onSuccess(resultado);
        });
    }

    private <T> void enviarErrorAlHiloPrincipal(ApiCallback<T> callback, String mensaje) {
        manejadorUI.post(() -> {
            if (callback != null)
                callback.onError(mensaje);
        });
    }
}
