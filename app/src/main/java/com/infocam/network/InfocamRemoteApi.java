package com.infocam.network;

import com.infocam.model.Camara;
import com.infocam.model.Incidencia;
import com.infocam.model.LoginRequest;
import com.infocam.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Esta interfaz NO contiene ningún código logico. Lo único que incluye son anotaciones que le dicen a Retrofit CÓMO CONSTRUIR las peticiones HTTP.
public interface InfocamRemoteApi {
    /* Esto es lo que le diremos a la librería:
    * 1. Qué ruta queremos utilizar.
    * 2. Qué método HTTP vamos a usar. Esto serían los GET, POST y PUT.
    * 3. Qué datos enviamos y cuáles esperamos recibir. */
        @POST("auth/login") // EJEMPLO: Enviaremos los datos de inicio de sesión.
        Call<Usuario> login(@Body LoginRequest credentials); // EJEMPLO: Convertiremos un objeto LoginRequest a JSON.

        @POST("auth/registro")
        Call<Void> registro(@Body Usuario usuario); // Cuando utilizamos "Void" en la llamada, no esperamos recibir datos, con un código de validación (200 o 201) nos vale.

        @GET("camaras/activas")
        Call<List<Camara>> getCamarasActivas(@Header("Authorization") String token); // Cuando utilizamos "@Header", estamos inyectando el Token de SessionManager, necesario para saber que el usuario es correcto.

        @POST("camaras/{idCamara}/favorita")
        Call<Void> toggleFavorito(
                        @Header("Authorization") String token,
                        @Path("idCamara") int idCamara, // Remplazamos el valor en la URL (le añadiremos el id de ESA cámara).
                        @Query("usuarioId") int idUsuario); // Añadiremos los parámetros al final de la URL (EJEMPLO: ?usuarioId=10).

        @GET("usuarios/{idUsuario}/favoritos/camaras")
        Call<List<Camara>> getFavoritosUsuario(
                        @Header("Authorization") String token,
                        @Path("idUsuario") int idUsuario);

        @GET("incidencias/tipos")
        Call<List<String>> getTiposIncidencia(@Header("Authorization") String token);

        @GET("incidencias/activas") // Sus parámetros de consulta serán la fecha y el identificador del usuario.
        Call<List<Incidencia>> getIncidenciasActivas(
                        @Header("Authorization") String token,
                        @Query("fecha") String fecha,
                        @Query("usuarioId") Integer idUsuario);

        @POST("incidencias")
        Call<Void> crearIncidencia(
                        @Header("Authorization") String token,
                        @Body Incidencia incidencia);

        @PUT("usuarios/{id}")
        Call<Usuario> actualizarUsuario(
                        @Header("Authorization") String token,
                        @Path("id") int id,
                        @Body Usuario usuario);
}
