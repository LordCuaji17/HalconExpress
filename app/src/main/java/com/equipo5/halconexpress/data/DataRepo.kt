package com.equipo5.halconexpress.data

import android.content.Context

// Simple wrapper to be called from Kotlin/Coroutines
object DataRepo {

    fun obtenerParadas(context: Context): List<Parada> {
        val dao = ParadasDAO(context)
        return dao.obtenerTodasLasParadas()
    }

    fun obtenerRutas(context: Context): List<Ruta> {
        val dao = RutasDAO(context)
        return dao.obtenerTodasLasRutas()
    }

    // ===============================================
    //           NUEVA FUNCIÓN: Módulo 5
    // ===============================================

    /**
     * Obtiene la lista de horarios de salida de una ruta específica
     * llamando a la función Java del RutasDAO.
     */
    fun getHorariosDeRuta(context: Context, idRuta: Int): List<String> {
        val dao = RutasDAO(context)
        // Llama a la función que ya existe en el DAO de Java
        return dao.obtenerHorariosPorRuta(idRuta)
    }
}