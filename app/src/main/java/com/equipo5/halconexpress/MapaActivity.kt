package com.equipo5.halconexpress

import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.equipo5.halconexpress.data.Parada
import com.equipo5.halconexpress.data.ParadasDAO

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    // ============================================================================================
    // VARIABLES Y CONSTANTES
    // ============================================================================================

    // Componentes del Mapa y Datos
    private lateinit var googleMap: GoogleMap
    private lateinit var paradasDAO: ParadasDAO
    private var paradas: List<Parada> = emptyList()

    // Elementos Gráficos en el Mapa
    private var polylineActual: Polyline? = null
    private var marcadorCamion: Marker? = null // Nota: Parece no usarse, se usa camionMarker
    private var camionMarker: Marker? = null
    private lateinit var iconoCamion: BitmapDescriptor

    // Variables de Control y Animación
    private var animacionActiva = false
    private var posITT = LatLng(19.2573852383869, -99.57761503386585) // Ubicación base (ITT)

    // ============================================================================================
    // CICLO DE VIDA (LIFECYCLE)
    // ============================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Inicializar base de datos
        paradasDAO = ParadasDAO(this)

        // Inicializar Fragmento del Mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar Botones
        setupBotones()
    }

    private fun setupBotones() {
        // NUEVO: Botón de Regreso
        // Buscamos la flecha por su ID (btnAtras) y le decimos que cierre la pantalla (finish)
        findViewById<View>(R.id.btnAtras).setOnClickListener {
            finish() // Esto cierra MapaActivity y te devuelve al Menú Principal
        }

        // Botón para cargar paradas
        findViewById<Button>(R.id.btnCargarRutas).setOnClickListener {
            cargarParadasYMostrarRutas()
        }

        // Botón para opciones de capa
        findViewById<Button>(R.id.btnOpcionesMapa).setOnClickListener { btn ->
            mostrarMenuOpciones(btn)
        }
    }

    // ============================================================================================
    // CONFIGURACIÓN DEL MAPA (ON MAP READY)
    // ============================================================================================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 1. Configuración de UI del mapa
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isScrollGesturesEnabledDuringRotateOrZoom = true
            isMapToolbarEnabled = true
        }

        // 2. Inicializar recursos gráficos
        iconoCamion = BitmapDescriptorFactory.fromResource(R.drawable.camion)
        posITT = LatLng(19.2573852383869, -99.57761503386585)

        // 3. Listener de Clic en Marcadores
        googleMap.setOnMarkerClickListener { marker ->
            val nombre = marker.title ?: return@setOnMarkerClickListener false

            // Si se hace clic en el ITT, no hacemos nada (es el origen)
            if (nombre.contains("Instituto Tecnológico de Toluca", true)) {
                return@setOnMarkerClickListener false
            }

            // Si es otro destino, iniciamos la animación hacia allá
            val destino = marker.position
            animarRutaSeleccionada(destino)
            true
        }

        // 4. Mover cámara al inicio (ITT)
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(posITT, 15f),
            1500,
            null
        )
    }

    // ============================================================================================
    // LÓGICA PRINCIPAL: CARGA DE DATOS Y RUTAS
    // ============================================================================================

    private fun cargarParadasYMostrarRutas() {
        paradas = paradasDAO.obtenerTodasLasParadas()

        if (paradas.isEmpty()) {
            Toast.makeText(this, "No hay paradas en la base de datos.", Toast.LENGTH_LONG).show()
            return
        }

        // Buscar la parada principal (Origen)
        val principal = paradas.find {
            it.getNombre().contains("Instituto Tecnológico de Toluca", true)
        } ?: run {
            Toast.makeText(this, "No se encontró la parada principal.", Toast.LENGTH_LONG).show()
            return
        }

        googleMap.clear() // Limpiar mapa previo

        val posPrincipal = LatLng(principal.getLatitud(), principal.getLongitud())

        // Agregar Marcador Principal (Azul)
        googleMap.addMarker(
            MarkerOptions()
                .position(posPrincipal)
                .title(principal.getNombre())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        // Builder para ajustar el zoom y que se vean todas las paradas
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(posPrincipal)

        // Iterar sobre las paradas destino
        for (p in paradas) {
            if (p.getId() == principal.getId()) continue // Saltar el origen

            val destino = LatLng(p.getLatitud(), p.getLongitud())
            boundsBuilder.include(destino)

            // Agregar marcador destino
            googleMap.addMarker(
                MarkerOptions()
                    .position(destino)
                    .title(p.getNombre())
            )

            // Trazar ruta estática inicial
            trazarRutaCallesInicio(posPrincipal, destino)
        }

        // Ajustar la cámara para mostrar todos los puntos
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 180),
            1500,
            null
        )
    }

    // ============================================================================================
    // SERVICIOS DE RED (GOOGLE DIRECTIONS API & VOLLEY)
    // ============================================================================================

    /**
     * Solicita la ruta para la animación del camión.
     * Al recibir respuesta, llama a [animarCamion].
     */
    private fun solicitarRutaParaAnimacion(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyB0HQuSquhtkpC4hF01Bgp1e6zrKJnxLKM"

        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() == 0) {
                    animacionActiva = false
                    return@JsonObjectRequest
                }

                // Obtener puntos codificados
                val points = routes.getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val decodedPath = decodePolyline(points)

                // Iniciar animación con la ruta decodificada
                animarCamion(decodedPath)
            },
            {
                animacionActiva = false
            }
        )
        queue.add(request)
    }

    /**
     * Solicita una ruta para dibujarla en rojo y mostrar distancia/tiempo (Al hacer click).
     */
    private fun solicitarRuta(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyB0HQuSquhtkpC4hF01Bgp1e6zrKJnxLKM"

        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() == 0) return@JsonObjectRequest

                val route = routes.getJSONObject(0)

                // Extraer Distancia y Tiempo
                val legs = route.getJSONArray("legs")
                val leg = legs.getJSONObject(0)
                val distanciaTxt = leg.getJSONObject("distance").getString("text")
                val duracionTxt  = leg.getJSONObject("duration").getString("text")

                Toast.makeText(
                    this,
                    "📏 Distancia: $distanciaTxt\n⏱ Tiempo: $duracionTxt",
                    Toast.LENGTH_LONG
                ).show()

                // Dibujar Polyline Roja
                val points = route.getJSONObject("overview_polyline").getString("points")
                val decodedPath = decodePolyline(points)

                val polylineOptions = PolylineOptions()
                    .addAll(decodedPath)
                    .width(10f)
                    .color(Color.RED)

                // Eliminar ruta anterior seleccionada si existe
                polylineActual?.remove()
                polylineActual = googleMap.addPolyline(polylineOptions)
            },
            {
                Toast.makeText(this, "Error al solicitar ruta", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    /**
     * Solicita una ruta para dibujarla con color aleatorio (Carga inicial).
     */
    private fun trazarRutaCallesInicio(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyB0HQuSquhtkpC4hF01Bgp1e6zrKJnxLKM"
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() == 0) return@JsonObjectRequest

                val points = routes.getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val decoded = decodePolyline(points)

                googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(decoded)
                        .width(8f)
                        .color(colorAleatorio()) // Color aleatorio
                )
            },
            {}
        )
        queue.add(request)
    }

    // Wrapper simple para solicitarRuta (usado internamente si se requiere)
    private fun trazarRutaCalles(origen: LatLng, destino: LatLng) {
        solicitarRuta(origen, destino)
    }

    // ============================================================================================
    // LÓGICA DE ANIMACIÓN
    // ============================================================================================

    /**
     * Prepara el marcador del camión y solicita la ruta para animarlo.
     */
    private fun animarRutaSeleccionada(destino: LatLng) {
        if (animacionActiva) return
        animacionActiva = true

        // Reinicia camión en ITT
        camionMarker?.remove()
        camionMarker = googleMap.addMarker(
            MarkerOptions()
                .position(posITT)
                .icon(iconoCamion)
                .flat(true)
        )

        solicitarRutaParaAnimacion(posITT, destino)
    }

    /**
     * Mueve el marcador punto por punto en un hilo secundario.
     */
    private fun animarCamion(path: List<LatLng>) {
        // Suavizar ruta interpolando puntos extra
        val rutaSuavizada = interpolarRuta(path, 5)

        Thread {
            for (i in rutaSuavizada.indices) {
                runOnUiThread {
                    camionMarker?.position = rutaSuavizada[i]

                    // Calcular rotación del camión según hacia dónde se mueve
                    if (i < rutaSuavizada.size - 1) {
                        val dir = bearing(rutaSuavizada[i], rutaSuavizada[i + 1])
                        camionMarker?.rotation = dir
                    }
                }
                Thread.sleep(5) // Velocidad de la animación
            }
            animacionActiva = false
        }.start()
    }

    /**
     * Interpola puntos adicionales entre coordenadas si la distancia es grande,
     * para que la animación se vea fluida.
     */
    private fun interpolarRuta(path: List<LatLng>, distanciaMinMetros: Int): List<LatLng> {
        val nueva = ArrayList<LatLng>()

        for (i in 0 until path.size - 1) {
            val p1 = path[i]
            val p2 = path[i + 1]

            nueva.add(p1)

            val distance = FloatArray(1)
            android.location.Location.distanceBetween(
                p1.latitude, p1.longitude,
                p2.latitude, p2.longitude,
                distance
            )

            val pasos = (distance[0] / distanciaMinMetros).toInt()

            for (s in 1 until pasos) {
                val lat = p1.latitude + (p2.latitude - p1.latitude) * (s.toDouble() / pasos)
                val lng = p1.longitude + (p2.longitude - p1.longitude) * (s.toDouble() / pasos)
                nueva.add(LatLng(lat, lng))
            }
        }
        nueva.add(path.last())
        return nueva
    }

    // ============================================================================================
    // UTILIDADES Y MENÚS
    // ============================================================================================

    private fun mostrarMenuOpciones(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_mapa, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.mapa_normal -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                R.id.mapa_satelite -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                R.id.mapa_hibrido -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                R.id.mapa_terreno -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                R.id.mapa_trafico -> {
                    googleMap.isTrafficEnabled = !googleMap.isTrafficEnabled
                    Toast.makeText(
                        this,
                        if (googleMap.isTrafficEnabled) "Tráfico activado" else "Tráfico desactivado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
        popup.show()
    }

    private fun colorAleatorio(): Int {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color.rgb(r, g, b)
    }

    /**
     * Calcula el ángulo de rotación entre dos coordenadas (para girar el icono del camión).
     */
    private fun bearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

        return (Math.toDegrees(Math.atan2(y, x)) + 360).rem(360).toFloat()
    }

    /**
     * Decodifica la cadena de puntos (Polyline Algorithm) que devuelve la API de Google.
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0

            // Latitude
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)

            // Longitude
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)

            poly.add(
                LatLng(
                    lat / 1E5,
                    lng / 1E5
                )
            )
        }
        return poly
    }

    // Helper para crear BitmapDescriptor desde recurso (presente en código original)
    private fun getBitmapDescriptor(resourceId: Int): BitmapDescriptor {
        val drawable = resources.getDrawable(resourceId, null)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}