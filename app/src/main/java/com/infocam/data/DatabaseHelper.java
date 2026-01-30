package com.infocam.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* Esta clase se encarga de hacer que los datos persistan en el dispositivo móvil. Para ello, se crea una base de datos local con SQLite.
 * Existen dos métodos principales, que hacen referencia al ciclo de vida de la aplicación:
 * 1. onCreate(): se ejecuta solo la primera vez que se crea la base de datos.
 * 2. onUpgrade(): se ejecuta cuando incrementamos el contador VERSION_BD (necesario cuando se modifica la estructura).*/
public class DatabaseHelper extends SQLiteOpenHelper {
    // Configuración básica de la BBDD.
    private static final String NOMBRE_BD = "InfoCam.db";
    private static final int VERSION_BD = 4;

    // Definición de la tabla de "Favoritos" y sus columnas, que será igual que en la BBDD a la que acude la API.
    public static final String TABLA_FAVORITOS = "favoritos";
    public static final String COL_ID = "idLocal";
    public static final String COL_ID_USUARIO = "idUsuario";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_DIRECCION = "direccion";
    public static final String COL_LAT = "latitud";
    public static final String COL_LNG = "longitud";
    public static final String COL_ID_CAMARA = "idCamara";
    public static final String COL_IMAGEN = "imagen";

    // Creamos la tabla de favoritos.
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

    // Se ejecuta cuando se crea la base de datos por primera vez.
    @Override
    public void onCreate(SQLiteDatabase bd) {
        bd.execSQL(SQL_CREACION_TABLA); // Llamamos a la query que crea la tabla.
    }

    // Se ejecuta cuando detecta que "VERSION_BD" ha cambiado.
    @Override
    public void onUpgrade(SQLiteDatabase bd, int versionAntigua, int versionNueva) {
        bd.execSQL("DROP TABLE IF EXISTS " + TABLA_FAVORITOS); // Borramos la tabla, si es que existe.
        onCreate(bd); // Llamamos al onCreate para que la vuelva a crear.
    }
}
