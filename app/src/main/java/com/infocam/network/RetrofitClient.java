package com.infocam.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Esta clase actúa como el "configurador" de Retrofit. Es la que se encarga de crear y configurar el propio objeto Retrofit.
public class RetrofitClient {
    // En este apartado definimos cuál es la dirección IP del servidor. Es importante que, una vez definida, el teléfono móvil y el ordenador que actúa como servidor estén en la misma red.
    private static final String BASE_URL = "http://10.10.16.85:8080/api/";
    private static Retrofit retrofit = null;

    // El método "getClient()" construte el cliente de red. En caso de que esté creado devuelve el que existe, si no lo fabrica desde cero.
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Hemos añadido el "HttpLoggingInterceptor" para poder ver en el Logcat todos los datos que entran y salen, cómo cuál es el JSON que enviamos o el código que devuelve el servidor.
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Le pediremos que nos muestre todo el cuerpo del mensaje, no solo una versión acortada.

            // Retrofit utiliza una librería OkHttp. Retrofit, por su cuenta, no sabe enviaar datos por internet, solo se encarga de traducir las interfaces de Java en peticiones que alguien más debe enviar. Aquí entra en juego OkHttp.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            // Por otro lado, GsonConverter actúa de traductor. Transforma todo el texto en JSON y viceversa.
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Le damos la dirección base
                    .addConverterFactory(GsonConverterFactory.create()) // Este es el momento en el que el JSON que recibimos del servidor se convierte en objetos Java.
                    .client(client) // Se enviará a través del motor OkHttp.
                    .build();
        }
        return retrofit;
    }
}
