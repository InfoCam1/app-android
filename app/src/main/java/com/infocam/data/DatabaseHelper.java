package com.infocam.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper: Clase encargada de la persistencia local mediante SQLite.
 * 
 * En el ciclo de vida de una app Android con base de datos:
 * 1. onCreate() se ejecuta solo la primera vez que se crea la base de datos.
 * 2. onUpgrade() se ejecuta cuando incrementamos VERSION_BD para modificar la
 * estructura.
 * 
 * Como alumnos de DAM, es vital entender que SQLite es una base de datos
 * relacional ligera incrustada en el dispositivo móvil.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Configuración básica
    private static final String NOMBRE_BD = "InfoCam.db";
    private static final int VERSION_BD = 4;

    // Definición de la TABLA de Favoritos y sus COLUMNAS
    public static final String TABLA_FAVORITOS = "favoritos";
    public static final String COL_ID = "idLocal";
    public static final String COL_ID_USUARIO = "idUsuario";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_DIRECCION = "direccion";
    public static final String COL_LAT = "latitud";
    public static final String COL_LNG = "longitud";
    public static final String COL_ID_CAMARA = "idCamara";
    public static final String COL_IMAGEN = "imagen";

    // Sentencia SQL para crear la tabla (DDL)
    private static final String SQL_CREACION_TABLA = "CREATE TABLE " + TABLA_FAVORITOS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ID_USUARIO + " INTEGER, " +
            COL_NOMBRE + " TEXT, " +
            COL_DIRECCION + " TEXT, " +
            COL_LAT + " REAL, " +
            COL_LNG + " REAL, " +
            COL_ID_CAMARA + " INTEGER, " +
            COL_IMAGEN + " TEXT" +
            ");";

    public DatabaseHelper(Context contexto) {
        super(contexto, NOMBRE_BD, null, VERSION_BD);
    }

    /**
     * Se ejecuta cuando se crea la base de datos físicamente por primera vez.
     */
    @Override
    public void onCreate(SQLiteDatabase bd) {
        bd.execSQL(SQL_CREACION_TABLA);
    }

    /**
     * Se ejecuta cuando detecta que VERSION_BD ha cambiado.
     * Útil para migraciones o cambios en el esquema.
     */
    @Override
    public void onUpgrade(SQLiteDatabase bd, int versionAntigua, int versionNueva) {
        // En un entorno de desarrollo, borramos y recreamos
        bd.execSQL("DROP TABLE IF EXISTS " + TABLA_FAVORITOS);
        onCreate(bd);
    }
}
