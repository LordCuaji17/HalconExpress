package com.equipo5.halconexpress

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
 * Este Composable sirve como "lanzador" (Launcher) para abrir la MapsActivity tradicional.
 *
 * Propósito: Cumplir con la convención del equipo (una función @Composable por pantalla)
 * y manejar la transición de Jetpack Compose (la interfaz) a la Activity de Google Maps.
 */
@Composable
fun PantallaMapa() {
    // 1. Obtener el contexto actual de la Activity o Composable.
    val context = LocalContext.current

    // 2. Ejecutar la lógica de lanzamiento inmediatamente.
    // Usamos el contexto para crear un Intent que apunta a MapsActivity.

    // Nota: Es importante que la MapsActivity se haya declarado correctamente en AndroidManifest.xml
    val intent = Intent(context, MapsActivity::class.java)
    context.startActivity(intent)

    // 3. Importante: Como esta pantalla no tiene UI (solo lanza otra Activity),
    // debemos manejar el retorno al menú en el MainActivity (ver paso 3).

    // No se dibuja nada aquí, solo se lanza la Activity.
    // Si necesitas un botón de "Cargando Mapa...", se podría añadir aquí.
}

/**
 * Función auxiliar para lanzar la Activity, separada por limpieza.
 */
fun lanzarMapsActivity(context: Context) {
    val intent = Intent(context, MapsActivity::class.java)
    context.startActivity(intent)
}