package com.equipo5.halconexpress.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class RutasDAO {

    private final HalconDataBase dbHelper;

    public RutasDAO(Context context) {
        dbHelper = new HalconDataBase(context);
    }

    // ==========================================
    //              ZONA DE RUTAS
    // ==========================================

    public long insertarRuta(String nombre, String descripcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        long id = db.insert(HalconDataBase.TABLE_RUTAS, null, values);
        db.close();
        return id;
    }

    public List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + HalconDataBase.TABLE_RUTAS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_Rutas"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
                lista.add(new Ruta(id, nombre, desc));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // ==========================================
    //            ZONA DE HORARIOS (¡AQUÍ ESTABA EL DETALLE!)
    // ==========================================

    // 1. Guardar el Horario
    public long insertarHorario(int idRuta, String horaSalida) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // OJO: Estas claves deben coincidir EXACTO con HalconDataBase.java
        values.put("id_Ruta", idRuta);
        values.put("hora_Salida", horaSalida);

        long id = db.insert(HalconDataBase.TABLE_HORARIOS, null, values);
        db.close();
        return id;
    }

    // 2. Leer los Horarios de una Ruta específica
    public List<String> obtenerHorariosPorRuta(int idRuta) {
        List<String> horarios = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta: "DAME la hora_Salida DE la tabla horarios DONDE el id_Ruta SEA igual al que te paso"
        Cursor cursor = db.rawQuery(
                "SELECT hora_Salida FROM " + HalconDataBase.TABLE_HORARIOS + " WHERE id_Ruta = ?",
                new String[]{String.valueOf(idRuta)}
        );

        if (cursor.moveToFirst()) {
            do {
                // Obtenemos el string de la columna 0 (que es hora_Salida)
                String hora = cursor.getString(0);
                horarios.add(hora);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return horarios;
    }
}