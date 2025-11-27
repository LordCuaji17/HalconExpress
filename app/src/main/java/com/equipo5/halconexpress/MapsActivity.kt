package com.equipo5.halconexpress

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.collections.forEachIndexed

/**
 * Módulo 1: Implementación de la Vista de Mapa.
 * Inicializa Google Maps, lo centra en el área de interés
 * y dibuja una ruta fija con paradas estáticas.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // Coordenadas simuladas para centrar el mapa (Ejemplo: Centro de Campus)
    private val campusCenter = LatLng(19.25740549542587, -99.57761503386585)
    private val defaultZoom = 15f // Nivel de zoom

    // DATOS ESTATICOS DE LA RUTA (HARDCODEADOS)
    private val paradasEstaticas = listOf(
        Pair("Entrada/Salida Estacionamiento 1", LatLng(19.25780312013838, -99.57734665909678)),
        Pair("Entrada/Salida Estacionamiento 2", LatLng(19.258096064571102, -99.58121690029449)),
        Pair("Entrada/Salida Estacionamiento 3", LatLng(19.25442006502898, -99.57767051451079)),
        Pair("Entrada/Salida Principal", LatLng(19.257132841310217, -99.57747171706087))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usa el layout que acabamos de definir
        setContentView(R.layout.activity_maps)

        // Obtiene el SupportMapFragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // Inicia la carga asíncrona del mapa
        mapFragment.getMapAsync(this)
    }

    /**
     * Este callback se activa cuando el mapa está listo para ser usado.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuración básica
        mMap.uiSettings.isZoomControlsEnabled = true

        // 1. Centrar la vista del mapa en el punto de interés.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, defaultZoom))

        // 2. Dibujar los marcadores y la polilínea de la ruta estática.
        drawMarkersAndRoute()
    }

    /**
     * Dibuja los marcadores de las paradas estáticas y traza la polilínea de la ruta.
     */
    private fun drawMarkersAndRoute() {
        val polylineOptions = PolylineOptions()
            .width(10f)
            .color(Color.BLUE) // Usamos Azul para la ruta estática
            .geodesic(true)

        paradasEstaticas.forEachIndexed { index, paradaData ->
            val (nombre, latLng) = paradaData

            // Agregar Marcador
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Parada ${index + 1}: $nombre")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            // Agregar punto a la Polilínea
            polylineOptions.add(latLng)
        }

        // Trazar la polilínea en el mapa.
        mMap.addPolyline(polylineOptions)

        Log.d("MapsActivity", "Módulo 1: Ruta estática y ${paradasEstaticas.size} paradas dibujadas.")
    }
}