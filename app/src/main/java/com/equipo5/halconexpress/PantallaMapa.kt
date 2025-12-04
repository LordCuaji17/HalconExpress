package com.equipo5.halconexpress

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * PantallaMapa
 * Este Composable actúa como un "puente" entre Jetpack Compose y la Activity tradicional (MapaActivity).
 * - Convención del equipo: una función @Composable por pantalla.
 * - Función principal: lanzar la MapaActivity que contiene Google Maps.
 * - Nota: MapaActivity debe estar declarada en AndroidManifest.xml.
 */
@Composable
fun PantallaMapa() {
    // Contexto actual de la aplicación (necesario para crear Intents).
    val context = LocalContext.current

    // Crear un Intent que apunte a MapaActivity y lanzarlo inmediatamente.
    val intent = Intent(context, MapaActivity::class.java)
    context.startActivity(intent)

    // ⚠️ Este Composable no dibuja UI propia.
    // Solo inicia la Activity y delega la interfaz a MapaActivity.
    // El retorno al menú se maneja en MainActivity.
}
/**
 * lanzarMapsActivity
 * Función auxiliar para iniciar MapaActivity desde cualquier parte del código.
 * Útil si se quiere lanzar el mapa sin depender de un Composable.
 * @param context Contexto desde el cual se lanza la Activity.
 */
fun lanzarMapsActivity(context: Context) {
    val intent = Intent(context, MapaActivity::class.java)
    context.startActivity(intent)
}
