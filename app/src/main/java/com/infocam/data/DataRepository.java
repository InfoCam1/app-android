package com.infocam.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.infocam.model.Camara;
import com.infocam.model.Favorito;
import java.util.ArrayList;
import java.util.List;

/* Esta clase funcionará como "mediador" entre la base de datos de SQLite y el servidor. A través de operaciones CRUD
 * (Create, Read, Update, Delete) sincronizará los datos con la BBDD. */
public class DataRepository {
    private final DatabaseHelper gestorBD;

    public DataRepository(Context contexto) {
        gestorBD = new DatabaseHelper(contexto);
    }

    // Guardamos un nuevo favorito en la BBDD de SQLite.
    public long insertarFavorito(Favorito favorito) {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put(DatabaseHelper.COL_ID_USUARIO, favorito.getIdUsuario());
        valores.put(DatabaseHelper.COL_NOMBRE, favorito.getNombre());
        valores.put(DatabaseHelper.COL_DIRECCION, favorito.getDireccion());
        valores.put(DatabaseHelper.COL_LAT, favorito.getLatitud());
        valores.put(DatabaseHelper.COL_LNG, favorito.getLongitud());
        valores.put(DatabaseHelper.COL_ID_CAMARA, favorito.getIdCamara());
        valores.put(DatabaseHelper.COL_IMAGEN, favorito.getUrlImagen());

        // Inyectamos los datos con el ContentValues.
        long idGenerado = bd.insert(DatabaseHelper.TABLA_FAVORITOS, null, valores);
        bd.close();
        return idGenerado;
    }

    /* Recuperamos los favoritos del usuario, realizando un SELECT con su idUsuario. Esto se usará, por ejemplo, cada vez que cerremos sesión.
    * No nos interesa que el siguiente usuario pueda ver los favoritos de la otra persona, por lo que cuando se genera un favorito en la BBDD local
    * también guardaremos un backup en el servidor. Posteriormente será este backup el que recuperemos. */
    public List<Favorito> obtenerFavoritosPorUsuario(int idUsuario) {
        List<Favorito> listaResultados = new ArrayList<>();
        SQLiteDatabase bd = gestorBD.getReadableDatabase();

        // Le añadiremos el condicional de la búsqueda: quiero que recojas los favoritos del usuario "x".
        String clausulaWhere = DatabaseHelper.COL_ID_USUARIO + " = ?";
        String[] argumentosWhere = { String.valueOf(idUsuario) };

        Cursor cursor = bd.query(
                DatabaseHelper.TABLA_FAVORITOS,
                null,
                clausulaWhere,
                argumentosWhere,
                null,
                null,
                null);

        // Leemos todos los datos para poder guardarlos en un objeto Favorito nuevo.
        if (cursor.moveToFirst()) {
            do {
                Favorito f = new Favorito();
                f.setIdLocal(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
                f.setIdUsuario(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID_USUARIO)));
                f.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)));
                f.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DIRECCION)));
                f.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT)));
                f.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LNG)));
                f.setIdCamara(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID_CAMARA)));
                f.setUrlImagen(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGEN)));
                listaResultados.add(f);
            } while (cursor.moveToNext());
        }

        cursor.close();
        bd.close();
        return listaResultados;
    }

    // Eliminamos un registro de favorito específico usando su ID de autoincremento. Esto nos servirá cuando queramos borrar un favorito concreto, ya sea del mapa como de la lista.
    public void eliminarFavorito(int idFavoritoLocal) {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.delete(DatabaseHelper.TABLA_FAVORITOS,
                DatabaseHelper.COL_ID + " = ?",
                new String[] { String.valueOf(idFavoritoLocal) });
        bd.close();
    }

    // También podremos borrar todos los favoritos locales del usuario. Así, podremos forzar una sincronización con el servidor tras volver a iniciar sesión.
    public void vaciarFavoritosDeUsuario(int idUsuario) {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.delete(DatabaseHelper.TABLA_FAVORITOS,
                DatabaseHelper.COL_ID_USUARIO + " = ?",
                new String[] { String.valueOf(idUsuario) });
        bd.close();
    }

    // Cuando hagamos login, lanzaremos una sincronización para actualizar la BBDD local y mantener la aplicación actualizada con los mismos datos que el servidor.
    public void sincronizarConServidor(int idUsuario, List<Camara> camarasFavoritasApi) {
        // Primero limpiamos la base de datos local para este usuario.
        vaciarFavoritosDeUsuario(idUsuario);

        // En caso de no tener ninguna cámara en favoritos (servidor), no seguiremos con esta sincronización.
        if (camarasFavoritasApi == null || camarasFavoritasApi.isEmpty())
            return;

        // Luego insertamos la información nueva que viene del API.
        for (Camara c : camarasFavoritasApi) {
            Favorito f = new Favorito();
            f.setIdUsuario(idUsuario);
            f.setIdCamara(c.getId());
            f.setNombre(c.getNombre());
            f.setDireccion("Vía pública");
            f.setLatitud(c.getLatitud());
            f.setLongitud(c.getLongitud());
            f.setUrlImagen(c.getImagen());
            insertarFavorito(f);
        }
    }
}
