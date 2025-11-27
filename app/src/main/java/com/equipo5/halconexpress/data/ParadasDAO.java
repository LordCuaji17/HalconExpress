package com.equipo5.halconexpress.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ParadasDAO {

    private final HalconDataBase dbHelper;

    public ParadasDAO(Context context) {
        dbHelper = new HalconDataBase(context);
    }

    // ==========================================
    //              ZONA DE PARADAS
    // ==========================================

    public List<Parada> obtenerTodasLasParadas() {
        List<Parada> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + HalconDataBase.TABLE_PARADAS + " ORDER BY nombre",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_Paradas"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("ubicacion"));
                double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
                double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));

                lista.add(new Parada(id, nombre, ubicacion, latitud, longitud));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public Parada obtenerParadaPorId(int idParada) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Parada parada = null;

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + HalconDataBase.TABLE_PARADAS + " WHERE id_Paradas = ?",
                new String[]{String.valueOf(idParada)}
        );

        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("ubicacion"));
            double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
            double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));

            parada = new Parada(idParada, nombre, ubicacion, latitud, longitud);
        }
        cursor.close();
        db.close();
        return parada;
    }

    // ==========================================
    //            ZONA DE RUTAS POR PARADA
    // ==========================================

    // Obtener rutas que pasan por una parada específica (VERSIÓN TEMPORAL)
    // Obtener rutas que pasan por una parada específica (VERSIÓN MEJORADA)
    public List<String> obtenerRutasPorParada(int idParada) {
        List<String> rutas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // Consulta TODAS las rutas existentes (sin relación con paradas)
            Cursor cursor = db.rawQuery(
                    "SELECT nombre FROM " + HalconDataBase.TABLE_RUTAS + " ORDER BY nombre",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    String nombreRuta = cursor.getString(0);
                    rutas.add(nombreRuta);
                } while (cursor.moveToNext());
            }
            cursor.close();


        } catch (Exception e) {
            // Si hay error, usa datos de prueba como respaldo
            rutas.add("Ruta Campus-Centro");
            rutas.add("Ruta Express");
        } finally {
            db.close();
        }

        return rutas;
    }

    public long insertarParada(String nombre, String ubicacion, double latitud, double longitud) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("nombre", nombre);
        values.put("ubicacion", ubicacion);
        values.put("latitud", latitud);
        values.put("longitud", longitud);

        long id = db.insert(HalconDataBase.TABLE_PARADAS, null, values);
        db.close();
        return id;
    }



    // ==========================================
//     MÉTODO TEMPORAL PARA LIMPIAR PRUEBAS
// ==========================================

    // ELIMINAR RUTAS DE PRUEBA - EJECUTAR UNA VEZ Y LUEGO BORRAR ESTE MÉTODO
    public void eliminarRutasDePrueba() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Lista de rutas de prueba a eliminar
        String[] rutasParaEliminar = {
                "Ruta Norte",
                "Ruta Sur",
                "Ruta Biblioteca",
                "Ruta Laboratorios"
        };

        for (String nombreRuta : rutasParaEliminar) {
            db.delete(
                    HalconDataBase.TABLE_RUTAS,
                    "nombre = ?",
                    new String[]{nombreRuta}
            );
        }

        db.close();
    }












}