package com.infocam.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.infocam.model.Camara;
import com.infocam.model.Favorito;
import java.util.ArrayList;
import java.util.List;

/**
 * DataRepository: Implementamos el Patrón REPOSITORIO.
 * 
 * Este patrón actúa como un mediador entre la fuente de datos (SQLite)
 * y el resto de la aplicación. Su objetivo es abstraer el origen de los datos
 * para que a la interfaz le de igual de dónde vienen.
 * 
 * Nota para DAM: Aquí realizamos las operaciones CRUD (Create, Read, Update,
 * Delete).
 */
public class DataRepository {

    private final DatabaseHelper gestorBD;

    public DataRepository(Context contexto) {
        gestorBD = new DatabaseHelper(contexto);
    }

    /**
     * Guarda un nuevo favorito en la base de datos local.
     */
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

        // El método insert evita inyección SQL gracias al uso de ContentValues
        long idGenerado = bd.insert(DatabaseHelper.TABLA_FAVORITOS, null, valores);
        bd.close();
        return idGenerado;
    }

    /**
     * Recupera todos los favoritos de un usuario filtrados por su ID.
     */
    public List<Favorito> obtenerFavoritosPorUsuario(int idUsuario) {
        List<Favorito> listaResultados = new ArrayList<>();
        SQLiteDatabase bd = gestorBD.getReadableDatabase();

        String clausulaWhere = DatabaseHelper.COL_ID_USUARIO + " = ?";
        String[] argumentosWhere = { String.valueOf(idUsuario) };

        // Usamos el método query que es más seguro y limpio que un rawQuery
        Cursor cursor = bd.query(
                DatabaseHelper.TABLA_FAVORITOS,
                null,
                clausulaWhere,
                argumentosWhere,
                null,
                null,
                null);

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

    /**
     * Elimina un registro de favorito específico usando su ID de autoincremento.
     */
    public void eliminarFavorito(int idFavoritoLocal) {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.delete(DatabaseHelper.TABLA_FAVORITOS,
                DatabaseHelper.COL_ID + " = ?",
                new String[] { String.valueOf(idFavoritoLocal) });
        bd.close();
    }

    /**
     * Borra todos los favoritos locales del usuario para forzar una sincronización
     * limpia.
     */
    public void vaciarFavoritosDeUsuario(int idUsuario) {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.delete(DatabaseHelper.TABLA_FAVORITOS,
                DatabaseHelper.COL_ID_USUARIO + " = ?",
                new String[] { String.valueOf(idUsuario) });
        bd.close();
    }

    /**
     * Vacía toda la tabla de favoritos (borrado global).
     */
    public void vaciarTodosLosFavoritos() {
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.delete(DatabaseHelper.TABLA_FAVORITOS, null, null);
        bd.close();
    }

    /**
     * Algoritmo de Sincronización:
     * Comparamos los datos del API con la base de datos local para mantener
     * la aplicación al día tras el login.
     */
    public void sincronizarConServidor(int idUsuario, List<Camara> camarasFavoritasApi) {
        if (camarasFavoritasApi == null)
            return;

        // Primero limpiamos la base de datos local para este usuario
        vaciarFavoritosDeUsuario(idUsuario);

        // Luego insertamos la información fresca que viene del API
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
