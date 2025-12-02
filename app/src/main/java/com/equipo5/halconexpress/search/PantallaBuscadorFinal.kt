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
import androidx.activity.compose.BackHandler

// 1. PANTALLA PRINCIPAL (Nombre único)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBuscadorFinal(
    context: Context,
    viewModel: SearchViewModel,
    onVolver: () -> Unit,
    onItemClick: (tipo: String, id: Int) -> Unit
) {
    BackHandler(onBack = onVolver)
    var tipoSeleccion by remember { mutableStateOf("ambas") }
    var texto by remember { mutableStateOf("") }
    val resultados by viewModel.items.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatos(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            Text(text = "Buscador", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it; viewModel.filtrar(it, tipoSeleccion) },
            label = { Text("Buscar...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipFiltro("Ambas", tipoSeleccion == "ambas") { tipoSeleccion = "ambas"; viewModel.filtrar(texto, tipoSeleccion) }
            ChipFiltro("Paradas", tipoSeleccion == "paradas") { tipoSeleccion = "paradas"; viewModel.filtrar(texto, tipoSeleccion) }
            ChipFiltro("Rutas", tipoSeleccion == "rutas") { tipoSeleccion = "rutas"; viewModel.filtrar(texto, tipoSeleccion) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(resultados) { item ->
                when (item) {
                    // 2. AQUI USAMOS LOS NUEVOS NOMBRES (FilaParada y FilaRuta)
                    is ItemSearch.ParadaItem -> {
                        FilaParada(item.parada) { id -> onItemClick("parada", id) }
                    }
                    is ItemSearch.RutaItem -> {
                        FilaRuta(item.ruta) { id -> onItemClick("ruta", id) }
                    }
                }
                Divider()
            }
        }
    }
}

// 3. FUNCIONES AUXILIARES CON NUEVOS NOMBRES PARA EVITAR CONFLICTOS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipFiltro(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) { { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
    )
}

// CAMBIO DE NOMBRE: ItemParada -> FilaParada
@Composable
fun FilaParada(p: Parada, onClick: (id: Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick(p.getId()) }.padding(12.dp)) {
        Text(text = p.getNombre(), style = MaterialTheme.typography.titleMedium)
        Text(text = p.getUbicacion(), style = MaterialTheme.typography.bodyMedium)
    }
}

// CAMBIO DE NOMBRE: ItemRuta -> FilaRuta
@Composable
fun FilaRuta(r: Ruta, onClick: (id: Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick(r.getId()) }.padding(12.dp)) {
        Text(text = r.getNombre(), style = MaterialTheme.typography.titleMedium)
        Text(text = r.getDescripcion(), style = MaterialTheme.typography.bodyMedium)
    }
}