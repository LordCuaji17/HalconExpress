package com.equipo5.halconexpress

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange // <--- EL ICONO NUEVO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo5.halconexpress.data.Ruta
import com.equipo5.halconexpress.data.RutasDAO

@Composable
fun PantallaAdminRutas(onVolver: () -> Unit) {
    val context = LocalContext.current
    val rutasDAO = remember { RutasDAO(context) }

    var nombreRuta by remember { mutableStateOf("") }
    var descRuta by remember { mutableStateOf("") }
    val listaRutas = remember { mutableStateListOf<Ruta>() }

    // Variables para el DIÁLOGO DE HORARIOS
    var rutaSeleccionada by remember { mutableStateOf<Ruta?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Cargar rutas al inicio
    LaunchedEffect(Unit) {
        listaRutas.clear()
        listaRutas.addAll(rutasDAO.obtenerTodasLasRutas())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // --- BARRA SUPERIOR CON BOTÓN REGRESAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1B2A))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onVolver() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gestión de Rutas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // --- FORMULARIO DE RUTA ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nueva Ruta", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = nombreRuta,
                        onValueChange = { nombreRuta = it },
                        label = { Text("Nombre (Ej. Centro)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descRuta,
                        onValueChange = { descRuta = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (nombreRuta.isNotEmpty()) {
                                rutasDAO.insertarRuta(nombreRuta, descRuta)
                                listaRutas.clear()
                                listaRutas.addAll(rutasDAO.obtenerTodasLasRutas())
                                nombreRuta = ""
                                descRuta = ""
                                Toast.makeText(context, "Ruta Agregada", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B2A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AGREGAR RUTA")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Toca una ruta para ver sus horarios:", fontSize = 14.sp, color = Color.Gray)

            // --- LISTA DE RUTAS ---
            LazyColumn {
                items(listaRutas) { ruta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // AQUÍ CUMPLIMOS EL REQUERIMIENTO:
                                // Al seleccionar, guardamos la ruta y abrimos el diálogo
                                rutaSeleccionada = ruta
                                mostrarDialogo = true
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ruta.nombre, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
                                Text(ruta.descripcion, fontSize = 12.sp, color = Color.Gray)
                            }
                            // USAMOS DATERANGE (CALENDARIO) QUE SÍ VIENE POR DEFECTO
                            Icon(Icons.Default.DateRange, contentDescription = "Horarios", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE HORARIOS (EL POP-UP) ---
    if (mostrarDialogo && rutaSeleccionada != null) {
        DialogHorarios(
            ruta = rutaSeleccionada!!,
            dao = rutasDAO,
            onDismiss = { mostrarDialogo = false }
        )
    }
}

@Composable
fun DialogHorarios(ruta: Ruta, dao: RutasDAO, onDismiss: () -> Unit) {
    val listaHorarios = remember { mutableStateListOf<String>() }
    var nuevoHorario by remember { mutableStateOf("") }

    // Cargar horarios al abrir el diálogo
    LaunchedEffect(ruta.id) {
        listaHorarios.addAll(dao.obtenerHorariosPorRuta(ruta.id))
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Horarios: ${ruta.nombre}") },
        text = {
            Column {
                // Lista de horarios existentes
                if (listaHorarios.isEmpty()) {
                    Text("No hay horarios asignados.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    LazyColumn(modifier = Modifier.height(100.dp)) {
                        items(listaHorarios) { hora ->
                            Text("• $hora", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider() // Ojo: En Material3 es HorizontalDivider, si falla usa Divider
                Spacer(modifier = Modifier.height(10.dp))

                // Agregar nuevo
                Text("Agregar Salida:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nuevoHorario,
                        onValueChange = { nuevoHorario = it },
                        placeholder = { Text("Ej. 07:00 AM") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (nuevoHorario.isNotEmpty()) {
                            dao.insertarHorario(ruta.id, nuevoHorario)
                            listaHorarios.clear()
                            listaHorarios.addAll(dao.obtenerHorariosPorRuta(ruta.id)) // Recargar
                            nuevoHorario = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color(0xFF0D1B2A))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CERRAR") }
        }
    )
}