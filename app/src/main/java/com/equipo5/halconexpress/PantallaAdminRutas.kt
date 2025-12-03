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
import androidx.compose.material.icons.filled.DateRange
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

/**
 * Pantalla principal para la gestión administrativa de rutas y sus horarios.
 * Permite agregar nuevas rutas y acceder al detalle de horarios de cada una.
 */
@Composable
fun PantallaAdminRutas(onVolver: () -> Unit) {
    val context = LocalContext.current
    val rutasDAO = remember { RutasDAO(context) }

    // Estados para los campos del formulario
    var nombreRuta by remember { mutableStateOf("") }
    var descRuta by remember { mutableStateOf("") }

    // Estado para controlar la validación visual del campo nombre
    var errorNombre by remember { mutableStateOf(false) }

    // Lista observable de rutas para la UI
    val listaRutas = remember { mutableStateListOf<Ruta>() }

    // Estados para el control del diálogo de horarios
    var rutaSeleccionada by remember { mutableStateOf<Ruta?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Carga inicial de datos desde la BD local
    LaunchedEffect(Unit) {
        listaRutas.clear()
        listaRutas.addAll(rutasDAO.obtenerTodasLasRutas())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Encabezado de la pantalla
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
            // Tarjeta de registro de nueva ruta
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nueva Ruta", fontWeight = FontWeight.Bold)

                    // Campo Nombre con validación de estado de error
                    OutlinedTextField(
                        value = nombreRuta,
                        onValueChange = {
                            nombreRuta = it
                            errorNombre = false // Reinicia el estado de error al editar
                        },
                        label = { Text("Nombre (Ej. Centro)") },
                        isError = errorNombre,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (errorNombre) {
                                Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descRuta,
                        onValueChange = { descRuta = it },
                        label = { Text("Descripción (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            // Validación: Evitar registros vacíos o solo espacios
                            if (nombreRuta.trim().isNotEmpty()) {
                                rutasDAO.insertarRuta(nombreRuta.trim(), descRuta.trim())

                                // Actualizar UI
                                listaRutas.clear()
                                listaRutas.addAll(rutasDAO.obtenerTodasLasRutas())

                                // Resetear formulario
                                nombreRuta = ""
                                descRuta = ""
                                errorNombre = false
                                Toast.makeText(context, "Ruta Agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                errorNombre = true
                                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
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

            // Listado de rutas existentes
            LazyColumn {
                items(listaRutas) { ruta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
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
                            Icon(Icons.Default.DateRange, contentDescription = "Administrar Horarios", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // Modal para administración de horarios
    if (mostrarDialogo && rutaSeleccionada != null) {
        DialogHorarios(
            ruta = rutaSeleccionada!!,
            dao = rutasDAO,
            onDismiss = { mostrarDialogo = false }
        )
    }
}

/**
 * Componente de diálogo para listar y agregar horarios a una ruta específica.
 * Incluye validación de formato de hora (HH:MM).
 */
@Composable
fun DialogHorarios(ruta: Ruta, dao: RutasDAO, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val listaHorarios = remember { mutableStateListOf<String>() }

    var nuevoHorario by remember { mutableStateOf("") }
    var errorHorario by remember { mutableStateOf(false) }

    // Carga de horarios asíncrona al abrir el diálogo
    LaunchedEffect(ruta.id) {
        listaHorarios.addAll(dao.obtenerHorariosPorRuta(ruta.id))
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Horarios: ${ruta.nombre}") },
        text = {
            Column {
                // Visualización de horarios actuales
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
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Formulario de nuevo horario
                Text("Agregar Salida (24h):", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nuevoHorario,
                        onValueChange = {
                            if (it.length <= 5) {
                                nuevoHorario = it
                                errorHorario = false
                            }
                        },
                        label = { Text("Ej. 14:30") },
                        placeholder = { Text("HH:MM") },
                        singleLine = true,
                        isError = errorHorario,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        // Validación mediante Expresión Regular para formato 24 horas (00:00 - 23:59)
                        val regexHora = Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")

                        if (nuevoHorario.matches(regexHora)) {
                            dao.insertarHorario(ruta.id, nuevoHorario)

                            // Refrescar lista
                            listaHorarios.clear()
                            listaHorarios.addAll(dao.obtenerHorariosPorRuta(ruta.id))

                            nuevoHorario = ""
                            errorHorario = false
                            Toast.makeText(context, "Horario Agregado", Toast.LENGTH_SHORT).show()
                        } else {
                            errorHorario = true
                            Toast.makeText(context, "Formato inválido", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar Horario",
                            tint = if (errorHorario) Color.Red else Color(0xFF0D1B2A)
                        )
                    }
                }

                // Mensaje de retroalimentación en caso de error de formato
                if (errorHorario) {
                    Text(
                        text = "Usa formato de 24 horas. Ej: 08:15 o 16:30",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CERRAR") }
        }
    )
}