package com.equipo5.halconexpress.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

// ESTE ARCHIVO ES EL "COMÚN".
// Instrucción para el equipo: "Nadie edite esto a menos que agreguen tablas nuevas".
public class HalconDataBase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NOMBRE = "halcon_express.db";

    // Nombres de tablas públicos para que los DAOs los usen
    public static final String TABLE_PARADAS = "paradas";
    public static final String TABLE_RUTAS = "rutas";
    public static final String TABLE_HORARIOS = "horarios";

    public HalconDataBase(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TABLA PARADAS (Aunque es del Módulo 3, la creamos aquí para que exista)
        db.execSQL("CREATE TABLE " + TABLE_PARADAS + "(" +
                "id_Paradas INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "ubicacion TEXT NOT NULL," +
                "latitud REAL," +
                "longitud REAL)");

        // TABLA RUTAS (Módulo 2)
        db.execSQL("CREATE TABLE " + TABLE_RUTAS + "(" +
                "id_Rutas INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "descripcion TEXT NOT NULL)");

        // TABLA HORARIOS (Módulo 2)
        db.execSQL("CREATE TABLE " + TABLE_HORARIOS + " (" +
                "id_Horario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_Ruta INTEGER NOT NULL, " +
                "hora_Salida TEXT, " +
                "FOREIGN KEY(id_Ruta) REFERENCES " + TABLE_RUTAS + "(id_Rutas)" +
                ")");

        db.execSQL("INSERT INTO " + TABLE_RUTAS + " (nombre, descripcion) VALUES ('Ruta ITT-Centro de Toluca', 'Ruta directa al centro de Toluca')");
        db.execSQL("INSERT INTO " + TABLE_RUTAS + " (nombre, descripcion) VALUES ('Ruta ITT-Centro de Metepec', 'Ruta directa al centro de Metepec')");
        db.execSQL("INSERT INTO " + TABLE_RUTAS + " (nombre, descripcion) VALUES ('Ruta ITT-Centro de San Mateo Atenco', 'Ruta directa al centro de San Mateo Atenco')");
        db.execSQL("INSERT INTO " + TABLE_RUTAS + " (nombre, descripcion) VALUES ('Ruta ITT-Aeropuerto', 'Ruta directa al Aeropuerto')");

        //19.257242504331035, -99.5776108305652
        // Insertar una Parada de prueba
        db.execSQL("INSERT INTO " + TABLE_PARADAS +
                " (nombre, ubicacion, latitud, longitud) VALUES ('Instituto Tecnológico de Toluca', 'Av Tecnológico 100-s/n, Agrícola, 52149 San Salvador Tizatlalli, Méx.', 19.257242504331035, -99.5776108305652)");

        db.execSQL("INSERT INTO " + TABLE_PARADAS +
                " (nombre, ubicacion, latitud, longitud) VALUES ('Centro Toluca', 'Manzana 024, Centro, 50000 Toluca de Lerdo, Méx.', 19.292707943556916, -99.65694010449269)");

        db.execSQL("INSERT INTO " + TABLE_PARADAS +
                " (nombre, ubicacion, latitud, longitud) VALUES ('Parada Metepec', 'Manzana 011, Espiritu Santo, 52140 Metepec, Méx.', 19.25106777232457, -99.6051373333396)");

        db.execSQL("INSERT INTO " + TABLE_PARADAS +
                " (nombre, ubicacion, latitud, longitud) VALUES ('Centro San Mateo Atenco', 'Av. Lic. Benito Juárez 302, San Miguel, 52104 San Mateo Atenco, Méx.', 19.264270337054665, -99.5296928588737)");

        db.execSQL("INSERT INTO " + TABLE_PARADAS +
                " (nombre, ubicacion, latitud, longitud) VALUES ('Aeropuerto', 'San Pedro Totoltepec, 50226 Toluca de Lerdo, Méx.', 19.341301644124673, -99.57155738832698)");


        // Insertar un Horario de prueba (Asumiendo que la ruta creada tiene ID 1)
        db.execSQL("INSERT INTO " + TABLE_HORARIOS + " (id_Ruta, hora_Salida) VALUES (1, '07:00 AM')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUTAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HORARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARADAS);
        onCreate(db);
    }
}