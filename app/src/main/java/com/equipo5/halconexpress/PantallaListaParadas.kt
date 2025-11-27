package com.equipo5.halconexpress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo5.halconexpress.data.Parada
import com.equipo5.halconexpress.data.ParadasDAO

@Composable
fun PantallaListaParadas(onVolver: () -> Unit) {
    val context = LocalContext.current
    val paradasDAO = remember { ParadasDAO(context) }

    var paradas by remember { mutableStateOf<List<Parada>>(emptyList()) }
    var paradaSeleccionada by remember { mutableStateOf<Parada?>(null) }
    var mostrarDetalle by remember { mutableStateOf(false) }

    // Cargar paradas al inicio
    LaunchedEffect(Unit) {


        // --- ELIMINAR RUTAS DE PRUEBA (EJECUTAR UNA VEZ) ---
        paradasDAO.eliminarRutasDePrueba()
        // --- LUEGO BORRAR ESTA LÍNEA ---




        paradas = paradasDAO.obtenerTodasLasParadas()
    }

    // --- NAVEGACIÓN ENTRE LISTA Y DETALLE ---
    if (mostrarDetalle && paradaSeleccionada != null) {
        PantallaDetalleParada(
            parada = paradaSeleccionada!!,
            paradasDAO = paradasDAO,
            onVolver = {
                mostrarDetalle = false
                paradaSeleccionada = null
            }
        )
    } else {
        // Pantalla principal de lista
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Barra superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1B2A))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lista de Paradas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Lista de paradas
            if (paradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay paradas disponibles")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(paradas) { parada ->
                        ItemParada(
                            parada = parada,
                            onClick = {
                                paradaSeleccionada = parada
                                mostrarDetalle = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemParada(parada: Parada, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF0D1B2A),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = parada.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B2A)
                )
                Text(
                    text = parada.ubicacion,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}