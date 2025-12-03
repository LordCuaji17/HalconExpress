package com.equipo5.halconexpress

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.equipo5.halconexpress.data.ParadasDAO
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.equipo5.halconexpress.data.Parada
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

/*
Módulo 2: Implementación de Trazado de Ruta Realista.
Se ha ELIMINADO la lista de coordenadas estáticas (polylinePoints).
Ahora TODA la información de la ruta (marcadores y línea) proviene de la Base de Datos.
*/
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope {

    // 1. CONFIGURACIÓN DE COROUTINE: Necesario para hacer llamadas de red
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var mMap: GoogleMap
    private lateinit var paradasDAO: ParadasDAO

    // API KEY LEÍDA DESDE METADATOS
    private var googleMapsApiKey: String? = null

    // Punto central del mapa para enfocar la vista inicial (ITT)
    private val campusCenter = LatLng(19.25740549542587, -99.57761503386585)
    private val defaultZoom = 15.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar la API Key antes de configurar la vista
        googleMapsApiKey = getApiKeyFromMetadata()

        setContentView(R.layout.activity_maps)

        // Configuración de la Barra Superior (Toolbar) y Botón de Regreso
        val toolbar: Toolbar = findViewById(R.id.toolbar_mapa)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        paradasDAO = ParadasDAO(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancelar coroutines cuando la Activity se destruye
    }

    /**
     * Intenta leer la clave de API desde el AndroidManifest.xml.
     */
    private fun getApiKeyFromMetadata(): String? {
        try {
            val appInfo = applicationContext.packageManager.getApplicationInfo(
                applicationContext.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            return appInfo.metaData.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al leer API Key del Manifest: ${e.message}")
            return null
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // 1. DIBUJAR MARCADORES
        val paradas = drawMapMarkers()

        // 2. DIBUJAR RUTA REALISTA (SOLO SI HAY AL MENOS 2 PUNTOS)
        val routePoints = paradas.map { LatLng(it.getLatitud(), it.getLongitud()) }

        if (routePoints.size >= 2 && googleMapsApiKey != null) {
            // La llamada a la API requiere al menos dos puntos y una clave
            launch {
                getAndDrawRoute(routePoints, googleMapsApiKey!!)
            }
        } else {
            // Si la DB no tiene suficientes puntos o falta la API Key, solo se muestran los marcadores.
            Log.w("MapsActivity", "No se puede dibujar la ruta realista: Se necesitan al menos 2 puntos de la DB o falta la API Key.")

            // Si hay puntos, se dibuja la polilínea simple como un fallback visual
            drawSimplePolyline(routePoints)
        }

        // 3. CENTRAR CÁMARA
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, defaultZoom))
    }

    /**
     * Dibuja una polilínea simple (línea recta) como un fallback.
     */
    private fun drawSimplePolyline(points: List<LatLng>) {
        if (points.size >= 2) {
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(8f)
                .color(Color.RED)
                .geodesic(true)
            mMap.addPolyline(polylineOptions)
            Log.w("MapsActivity", "Se dibujó una polilínea simple (recta) como fallback.")
        }
    }


    /**
     * Dibuja los marcadores de las paradas obtenidos de la DB.
     * @return Lista de Parada.
     */
    private fun drawMapMarkers(): List<Parada> {
        val paradasDB: List<Parada> = try {
            paradasDAO.obtenerTodasLasParadas()
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al obtener paradas de la DB: ${e.message}")
            emptyList()
        }

        paradasDB.forEach { parada ->
            val latLng = LatLng(parada.getLatitud(), parada.getLongitud())

            val markerTitle = "Parada DB: ${parada.getNombre()}"

            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(markerTitle)
                    .snippet("Ubicación: ${parada.getUbicacion()}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            Log.d("MapsActivity", "Marcador dibujado: $markerTitle")
        }

        Log.d("MapsActivity", "Total de marcadores dibujados desde DB: ${paradasDB.size}")
        return paradasDB
    }

    /**
     * Hace la llamada asíncrona al Google Directions API y dibuja la polilínea.
     */
    private suspend fun getAndDrawRoute(points: List<LatLng>, apiKey: String) = withContext(Dispatchers.IO) {

        // 1. Construcción de Parámetros
        val origin = "origin=${points.first().latitude},${points.first().longitude}"
        val destination = "destination=${points.last().latitude},${points.last().longitude}"

        // Waypoints: Puntos intermedios (excluye inicio y fin)
        val waypoints = points.subList(1, points.size - 1)
            .joinToString("|") { "${it.latitude},${it.longitude}" }
            .let { if (it.isNotEmpty()) "waypoints=$it" else "" }

        val sensor = "sensor=false"
        val mode = "mode=walking"
        val key = "key=$apiKey"

        // 2. Construcción de URL
        val urlString = "https://maps.googleapis.com/maps/api/directions/json?$origin&$destination&$waypoints&$sensor&$mode&$key"

        Log.d("MapsActivity", "URL de Directions API: $urlString")

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            // 3. Lectura de Respuesta
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }

            // 4. Parsear y Dibujar en el hilo principal
            withContext(Dispatchers.Main) {
                val polyline = parseDirectionsResponse(response)
                if (polyline.isNotEmpty()) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .addAll(polyline)
                            .width(15f)
                            .color(Color.BLUE) // Color para la ruta realista
                            .geodesic(true)
                    )
                    Log.d("MapsActivity", "Ruta realista dibujada con ${polyline.size} puntos.")
                } else {
                    Log.w("MapsActivity", "No se pudo extraer la polilínea de la respuesta del Directions API.")
                    drawSimplePolyline(points) // Fallback: línea recta
                }
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error en la llamada al Directions API: ${e.message}. Se usa fallback.")
            withContext(Dispatchers.Main) {
                drawSimplePolyline(points) // Fallback: línea recta
            }
        }
    }

    /**
     * Parsea la respuesta JSON del Directions API para decodificar la polilínea.
     */
    private fun parseDirectionsResponse(jsonResponse: String): List<LatLng> {
        try {
            val json = JSONObject(jsonResponse)
            val routes = json.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                val encodedPolyline = overviewPolyline.getString("points")

                return decodePoly(encodedPolyline)
            }
            Log.w("MapsActivity", "API de Direcciones respondió, pero no encontró rutas.")
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al parsear la respuesta JSON: ${e.message}")
        }
        return emptyList()
    }

    /**
     * Utilidad para decodificar el formato de polilínea codificada de Google.
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }
}