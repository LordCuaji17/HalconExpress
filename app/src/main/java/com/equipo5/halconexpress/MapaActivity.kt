package com.equipo5.halconexpress

import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

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
    // VARIABLES
    // ============================================================================================

    private lateinit var googleMap: GoogleMap
    private lateinit var paradasDAO: ParadasDAO
    private var paradas: List<Parada> = emptyList()

    private var camionMarker: Marker? = null
    private lateinit var iconoCamion: BitmapDescriptor
    private var animacionActiva = false
    private var posITT = LatLng(19.2573852383869, -99.57761503386585)

    // UI de la Tarjeta
    private lateinit var cardInfoParada: CardView
    private lateinit var txtNombreParada: TextView
    private lateinit var txtUbicacionParada: TextView
    private lateinit var txtTiempoViaje: TextView
    private lateinit var txtDistanciaViaje: TextView
    private lateinit var btnCerrarInfo: Button

    // ============================================================================================
    // CICLO DE VIDA
    // ============================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        paradasDAO = ParadasDAO(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupBotones()
    }

    private fun setupBotones() {
        // 1. Inicializar Vistas de la Tarjeta
        cardInfoParada = findViewById(R.id.cardInfoParada)
        txtNombreParada = findViewById(R.id.txtNombreParada)
        txtUbicacionParada = findViewById(R.id.txtUbicacionParada)
        txtTiempoViaje = findViewById(R.id.txtTiempoViaje)
        txtDistanciaViaje = findViewById(R.id.txtDistanciaViaje)
        btnCerrarInfo = findViewById(R.id.btnCerrarInfo)

        btnCerrarInfo.setOnClickListener {
            cardInfoParada.visibility = View.GONE
        }

        // 2. Navegación y Acciones
        findViewById<View>(R.id.btnAtras).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCargarRutas).setOnClickListener { cargarParadasYMostrarRutas() }
        findViewById<Button>(R.id.btnOpcionesMapa).setOnClickListener { mostrarMenuOpciones(it) }
    }

    // ============================================================================================
    // MAPA
    // ============================================================================================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Icono del camión
        try {
            iconoCamion = BitmapDescriptorFactory.fromResource(R.drawable.camion)
        } catch (e: Exception) {
            iconoCamion = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
        }

        posITT = LatLng(19.2573852383869, -99.57761503386585)

        // CLICK EN MARCADOR
        googleMap.setOnMarkerClickListener { marker ->

            // A) Mostrar Info en Tarjeta
            val paradaObj = marker.tag as? Parada

            if (paradaObj != null) {
                txtNombreParada.text = paradaObj.getNombre()
                txtUbicacionParada.text = paradaObj.getUbicacion()

                // Ponemos "Calculando..." mientras llega la respuesta de Google
                txtTiempoViaje.text = "Calculando..."
                txtDistanciaViaje.text = "..."

                cardInfoParada.visibility = View.VISIBLE
            } else {
                // Caso ITT
                if (marker.title?.contains("Instituto Tecnológico", true) == true) {
                    txtNombreParada.text = "Tecnológico de Toluca"
                    txtUbicacionParada.text = "Origen"
                    txtTiempoViaje.text = "0 min"
                    txtDistanciaViaje.text = "0 km"
                    cardInfoParada.visibility = View.VISIBLE
                } else {
                    cardInfoParada.visibility = View.GONE
                }
            }

            // B) Solicitar Ruta y Animación
            val nombre = marker.title ?: return@setOnMarkerClickListener false
            if (!nombre.contains("Instituto Tecnológico de Toluca", true)) {
                val destino = marker.position
                animarRutaSeleccionada(destino)
            }
            true
        }

        googleMap.setOnMapClickListener { cardInfoParada.visibility = View.GONE }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posITT, 15f), 1500, null)
    }

    // ============================================================================================
    // DATOS
    // ============================================================================================

    private fun cargarParadasYMostrarRutas() {
        paradas = paradasDAO.obtenerTodasLasParadas()

        if (paradas.isEmpty()) {
            Toast.makeText(this, "No hay paradas.", Toast.LENGTH_LONG).show()
            return
        }

        val principal = paradas.find { it.getNombre().contains("Instituto Tecnológico de Toluca", true) }
        if (principal == null) return

        googleMap.clear()
        cardInfoParada.visibility = View.GONE

        val posPrincipal = LatLng(principal.getLatitud(), principal.getLongitud())
        googleMap.addMarker(
            MarkerOptions().position(posPrincipal).title(principal.getNombre())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(posPrincipal)

        for (p in paradas) {
            if (p.getId() == principal.getId()) continue

            val destino = LatLng(p.getLatitud(), p.getLongitud())
            boundsBuilder.include(destino)

            val marker = googleMap.addMarker(
                MarkerOptions().position(destino).title(p.getNombre())
            )
            marker?.tag = p // Guardamos info

            trazarRutaCallesInicio(posPrincipal, destino)
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 180), 1500, null)
    }

    // ============================================================================================
    // API GOOGLE & ANIMACIÓN
    // ============================================================================================

    // ESTA ES LA FUNCIÓN QUE FALTABA
    private fun animarRutaSeleccionada(destino: LatLng) {
        // Simplemente llama a la función de solicitud pasándole el origen (ITT)
        solicitarRutaParaAnimacion(posITT, destino)
    }

    private fun solicitarRutaParaAnimacion(origen: LatLng, destino: LatLng) {
        // Nota: Solo se ejecuta si NO hay animación activa para no saturar
        if (animacionActiva) return
        animacionActiva = true

        // Reinicia marcador camión
        camionMarker?.remove()
        camionMarker = googleMap.addMarker(
            MarkerOptions().position(posITT).icon(iconoCamion).flat(true)
        )

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyB0HQuSquhtkpC4hF01Bgp1e6zrKJnxLKM"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() == 0) {
                    animacionActiva = false
                    return@JsonObjectRequest
                }

                val routeObj = routes.getJSONObject(0)

                // 1. OBTENER TIEMPO Y DISTANCIA
                try {
                    val legs = routeObj.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    val distanciaTexto = leg.getJSONObject("distance").getString("text")
                    val duracionTexto = leg.getJSONObject("duration").getString("text")

                    // Actualizamos la UI con los datos reales
                    txtTiempoViaje.text = duracionTexto
                    txtDistanciaViaje.text = distanciaTexto

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. OBTENER PUNTOS Y ANIMAR
                val points = routeObj.getJSONObject("overview_polyline").getString("points")
                val decodedPath = decodePolyline(points)
                animarCamion(decodedPath)
            },
            {
                animacionActiva = false
                txtTiempoViaje.text = "Error"
            }
        )
        queue.add(request)
    }

    private fun animarCamion(path: List<LatLng>) {
        val rutaSuavizada = interpolarRuta(path, 5)
        Thread {
            for (i in rutaSuavizada.indices) {
                runOnUiThread {
                    camionMarker?.position = rutaSuavizada[i]
                    if (i < rutaSuavizada.size - 1) {
                        camionMarker?.rotation = bearing(rutaSuavizada[i], rutaSuavizada[i + 1])
                    }
                }
                Thread.sleep(5)
            }
            animacionActiva = false
        }.start()
    }

    // ============================================================================================
    // UTILS
    // ============================================================================================

    private fun trazarRutaCallesInicio(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyB0HQuSquhtkpC4hF01Bgp1e6zrKJnxLKM"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                    googleMap.addPolyline(PolylineOptions().addAll(decodePolyline(points)).width(8f).color(colorAleatorio()))
                }
            }, {}
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun interpolarRuta(path: List<LatLng>, distanciaMinMetros: Int): List<LatLng> {
        val nueva = ArrayList<LatLng>()
        for (i in 0 until path.size - 1) {
            val p1 = path[i]
            val p2 = path[i + 1]
            nueva.add(p1)
            val res = FloatArray(1)
            android.location.Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, res)
            val pasos = (res[0] / distanciaMinMetros).toInt()
            for (s in 1 until pasos) {
                val f = s.toDouble() / pasos
                nueva.add(LatLng(p1.latitude + (p2.latitude - p1.latitude) * f, p1.longitude + (p2.longitude - p1.longitude) * f))
            }
        }
        nueva.add(path.last())
        return nueva
    }

    private fun mostrarMenuOpciones(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_mapa, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mapa_normal -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                R.id.mapa_satelite -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                R.id.mapa_hibrido -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                R.id.mapa_terreno -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                R.id.mapa_trafico -> {
                    googleMap.isTrafficEnabled = !googleMap.isTrafficEnabled
                    Toast.makeText(this, "Tráfico: ${if(googleMap.isTrafficEnabled) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    private fun colorAleatorio(): Int {
        return Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }

    private fun bearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        return (Math.toDegrees(Math.atan2(y, x)) + 360).rem(360).toFloat()
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0; var lat = 0; var lng = 0
        while (index < encoded.length) {
            var b: Int; var shift = 0; var result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1F) shl shift); shift += 5 } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            shift = 0; result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1F) shl shift); shift += 5 } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}