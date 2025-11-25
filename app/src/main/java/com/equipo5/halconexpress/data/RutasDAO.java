package com.equipo5.halconexpress.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

// DAO = Data Access Object
// Este archivo contiene SOLO la lógica para el MÓDULO 2 (Gestión Admin)
public class RutasDAO {

    private final HalconDataBase dbHelper;

    public RutasDAO(Context context) {
        // Inicializamos la conexión común
        dbHelper = new HalconDataBase(context);
    }

    // --- FUNCIONES PARA RUTAS ---

    public long insertarRuta(String nombre, String descripcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        // Cerramos la conexión tras escribir para liberar memoria
        long id = db.insert(HalconDataBase.TABLE_RUTAS, null, values);
        db.close();
        return id;
    }

    // --- FUNCIONES PARA HORARIOS ---

    public long insertarHorario(int idRuta, String horaSalida) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_Ruta", idRuta);
        values.put("hora_Salida", horaSalida);
        long id = db.insert(HalconDataBase.TABLE_HORARIOS, null, values);
        db.close();
        return id;
    }

    // Obtener lista de horarios de una ruta (Para tu pantalla de Admin)
    public ArrayList<String> obtenerHorariosPorRuta(int idRuta) {
        ArrayList<String> horarios = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT hora_Salida FROM " + HalconDataBase.TABLE_HORARIOS + " WHERE id_Ruta = ?",
                new String[]{String.valueOf(idRuta)}
        );

        if (cursor.moveToFirst()) {
            do {
                horarios.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return horarios;
    }

    // Aquí puedes agregar métodos como 'eliminarRuta', 'editarHorario', etc.
}