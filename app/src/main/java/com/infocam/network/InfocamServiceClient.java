package com.infocam.network;

import com.infocam.model.Camara;
import com.infocam.model.Incidencia;
import com.infocam.model.LoginRequest;
import com.infocam.model.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Esta clase es una de las más importantes de toda la aplicación. Actúa como "puente" con el resto de la aplicación: esta le consulará los datos que
 * quiera conocer, mientras que InfocamServiceClient (al conocer cómo usar la interfaz de Retrofit InfocamRemoteApi) será la que hable con el servidor.
 */
public class InfocamServiceClient {
    // Crearemos las variables necesarias para utilizar la librería de Retrofit.
    private static InfocamServiceClient instanciaUnica;
    private final InfocamRemoteApi infocamRemoteApi;

    // Con este constructor privado accederemos a la configuración base (Retrofit.getClient() e implementaremos automáticamente los métodos de la interfaz (.create)).
    private InfocamServiceClient() {
        infocamRemoteApi = RetrofitClient.getClient().create(InfocamRemoteApi.class);
    }

    // Para evitar que cada parte de la app utilice un cliente de red distinto, crearemos un método "synchronized" que obligue a la app a utilizar el mismo (solo una parte podrá entrar cada vez).
    public static synchronized InfocamServiceClient obtenerInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new InfocamServiceClient();
        }
        return instanciaUnica;
    }

    // |------------------------------------------------------------------------------|
    // | GESTIÓN DE USUARIOS                                                          |
    // |------------------------------------------------------------------------------|

    public void iniciarSesion(String nombreUsuario, String contrasena, ApiCallback<Usuario> callback) {
        // "Empaquetamos" los datos en un objeto LoginRequest. Esto nos ayudará a cerciorarnos de que únicamente el usuario y la contraseña se pasan al servidor, no todo el Usuario.
        LoginRequest credentials = new LoginRequest(nombreUsuario, contrasena);

        // ".enqueue()" lanza la petición en segundo plano, para evitar que la aplicación se congele.
        infocamRemoteApi.login(credentials).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) { // Si entra en el if, es que el intercambio de información ha sido satisfactorio (código 200, 201...).
                    callback.onSuccess(response.body()); // Si ha sido un éxito, iremos al mapa principal.
                } else {
                    callback.onError("Credenciales incorrectas (Código: " + response.code() + ")"); // En caso de que los datos no coincidan con los del servidor, lanzaremos un mensaje de error que avise al usuario.
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) { // Si se lanza "onFailure", es que no se ha podido conectar con el servidor correctamente.
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void registrarUsuario(Usuario usuario, ApiCallback<Void> callback) {
        infocamRemoteApi.registro(usuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null); // El registro ha sido correcto, pero no esperamos recibir datos del servidor.
                } else {
                    callback.onError("No se pudo completar el registro.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void actualizarUsuario(String token, int id, String nombre, String email, long telefono, String password,
            ApiCallback<Usuario> callback) {
            // Creamos un objeto Usuario con los nuevos datos que hemos introducido en el menú "Perfil".
                Usuario update = new Usuario();
                update.setNombre(nombre);
                update.setEmail(email);
                update.setTelefono(telefono);
                if (password != null && !password.isEmpty()) {
                    update.setPassword(password);
                }
        // Es muy importante añadir "Bearer", ya que es el estándar de seguridad para el token. Si no utilizamos ningún token (que asegura que somos la persona autorizada), el servidor puede bloquear la petición al pensar que se trata de un hackeo o similar.
        infocamRemoteApi.actualizarUsuario("Bearer " + token, id, update).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al actualizar los datos en el servidor.");
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // |------------------------------------------------------------------------------|
    // | GESTIÓN DE CÁMARAS Y FAVORITOS                                               |
    // |------------------------------------------------------------------------------|

    public void obtenerCamarasActivas(String token, ApiCallback<List<Camara>> callback) {
        infocamRemoteApi.getCamarasActivas("Bearer " + token).enqueue(new Callback<List<Camara>>() {
            @Override
            public void onResponse(Call<List<Camara>> call, Response<List<Camara>> response) {
                // En este punto, Retrofit ya ha convertido el JSON en una List<Camara> de manera automática.
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al descargar cámaras.");
                }
            }

            @Override
            public void onFailure(Call<List<Camara>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // En nuestra base de datos, los favoritos son tratados como un toggle. Pueden estar "encendidos" o "apagados".
    public void conmutarFavorito(String token, int idCamara, int idUsuario, ApiCallback<Void> callback) {
        infocamRemoteApi.toggleFavorito("Bearer " + token, idCamara, idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("No se pudo cambiar el estado de favorito.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void obtenerFavoritosUsuario(String token, int idUsuario, ApiCallback<List<Camara>> callback) {
        infocamRemoteApi.getFavoritosUsuario("Bearer " + token, idUsuario).enqueue(new Callback<List<Camara>>() {
            @Override
            public void onResponse(Call<List<Camara>> call, Response<List<Camara>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al sincronizar favoritos.");
                }
            }

            @Override
            public void onFailure(Call<List<Camara>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // |------------------------------------------------------------------------------|
    // | GESTIÓN DE INCIDENCIAS                                                       |
    // |------------------------------------------------------------------------------|

    public void obtenerTiposIncidencia(String token, ApiCallback<List<String>> callback) {
        infocamRemoteApi.getTiposIncidencia("Bearer " + token).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener tipos de incidencia.");
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void obtenerIncidencias(String token, Integer idUsuario, ApiCallback<List<Incidencia>> callback) {
        // Debido al formato de fecha que manda la API Tráfico, debemos formatearla en nuestro código para responder a lo que espera el servidor. En este caso utiliza un formato ISO 8601.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        String fechaActual = sdf.format(new Date());

        infocamRemoteApi.getIncidenciasActivas("Bearer " + token, fechaActual, idUsuario)
                .enqueue(new Callback<List<Incidencia>>() {
                    @Override
                    public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error al obtener incidencias.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void crearIncidencia(String token, Incidencia incidencia, ApiCallback<Void> callback) {
        infocamRemoteApi.crearIncidencia("Bearer " + token, incidencia).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error al reportar la incidencia.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
