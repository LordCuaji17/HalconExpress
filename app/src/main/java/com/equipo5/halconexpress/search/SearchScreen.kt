package com.equipo5.halconexpress.search

import android.content.Context
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
import androidx.compose.ui.unit.dp
import com.equipo5.halconexpress.data.Parada
import com.equipo5.halconexpress.data.Ruta
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PantallaBuscador(
    context: Context,
    viewModel: SearchViewModel,
    onVolver: () -> Unit,
    onItemClick: (tipo: String, id: Int) -> Unit // tipo: "parada" o "ruta"
) {
    var tipoSeleccion by remember { mutableStateOf("ambas") } // "paradas", "rutas", "ambas"
    var texto by remember { mutableStateOf("") }
    val items by viewModel.items.collectAsState()

    // cargar datos la primera vez
    LaunchedEffect(Unit) {
        viewModel.cargarDatos(context)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)) {

        // header con flecha volver
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(text = "Buscador", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // campo búsqueda
        OutlinedTextField(
            value = texto,
            onValueChange = {
                texto = it
                viewModel.filtrar(it, tipoSeleccion)
            },
            label = { Text("Buscar parada o ruta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle simple para elegir tipo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipSimple(text = "Paradas", selected = tipoSeleccion == "paradas") {
                tipoSeleccion = "paradas"
                viewModel.filtrar(texto, tipoSeleccion)
            }
            FilterChipSimple(text = "Rutas", selected = tipoSeleccion == "rutas") {
                tipoSeleccion = "rutas"
                viewModel.filtrar(texto, tipoSeleccion)
            }
            FilterChipSimple(text = "Ambas", selected = tipoSeleccion == "ambas") {
                tipoSeleccion = "ambas"
                viewModel.filtrar(texto, tipoSeleccion)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de resultados
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items) { item ->
                when (item) {
                    is ItemSearch.ParadaItem -> {
                        ItemParada(item.parada) {
                            onItemClick("parada", it)
                        }
                    }
                    is ItemSearch.RutaItem -> {
                        ItemRuta(item.ruta) {
                            onItemClick("ruta", it)
                        }
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
fun FilterChipSimple(text: String, selected: Boolean, onClick: () -> Unit) {
    ElevatedFilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
    )
}

@Composable
fun ItemParada(p: Parada, onClick: (id: Int) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick(p.getId()) }
        .padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = p.getNombre(), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = p.getUbicacion(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ItemRuta(r: Ruta, onClick: (id: Int) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick(r.getId()) }
        .padding(12.dp)) {
        Text(text = r.getNombre(), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = r.getDescripcion(), style = MaterialTheme.typography.bodyMedium)
    }
}