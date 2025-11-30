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
}