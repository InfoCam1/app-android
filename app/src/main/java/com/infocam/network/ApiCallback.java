package com.infocam.network;

/* Las peticiones al servidor no son instantáneas. Para evitar el bloqueo de la aplicación mientras esperamos los datos, utilizaremos esta interfaz.
 * Es genérica, es decir, nos sirve para cualquier objeto que queramos recibir.
 * @param <T> Tipo de dato esperado (Usuario, List<Camara>, etc.)
 * La clase InfocamServiceClient utiliza Retrofit, traduce la respuesta que recibe y lo pasa a esta clase. Así, hace de enlace entre Retrofit y la propia interfaz.*/
public interface ApiCallback<T> {
    // Esto se ejecutará si ha recibido un código 200 (que todo ha ido bien).
    void onSuccess(T result); // Le llegará el objeto convertido de JSON a Java.
    // En caso de que algo falle, devolveremos un mensaje de error que se pueda entender. En nuestro caso utilizaremos principalmente Toast.
    void onError(String error);
}
