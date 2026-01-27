package com.infocam.network;

/**
 * Interfaz genérica para callbacks de la API.
 * Se usa para devolver resultados al hilo principal desde el ExecutorService.
 * 
 * @param <T> Tipo de dato esperado (Usuario, List<Camara>, etc.)
 */
public interface ApiCallback<T> {
    void onSuccess(T result);

    void onError(String error);
}
