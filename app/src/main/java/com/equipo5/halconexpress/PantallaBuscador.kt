package com.equipo5.halconexpress

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo5.halconexpress.data.DataRepo
import com.equipo5.halconexpress.data.Parada
import com.equipo5.halconexpress.data.Ruta
import com.equipo5.halconexpress.search.ItemSearch

@Composable
fun PantallaBuscador(
    context: Context,
    onVolver: () -> Unit
) {
    // --- ESTADOS ---
    var query by remember { mutableStateOf("") }
    var tipoFiltro by remember { mutableStateOf("ambas") }

    // caches
    var paradas by remember { mutableStateOf(emptyList<Parada>()) }
    var rutas by remember { mutableStateOf(emptyList<Ruta>()) }

    // resultados
    var items by remember { mutableStateOf<List<ItemSearch>>(emptyList()) }

    // --- CARGAR DB SOLO UNA VEZ ---
    LaunchedEffect(Unit) {
        paradas = DataRepo.obtenerParadas(context)
        rutas = DataRepo.obtenerRutas(context)
        items = buildCombined(paradas, rutas)
    }

    // Aquí usamos SOLO tu theme existente (MaterialTheme.colorScheme / typography)
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Buscador",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        // --- BARRA DE BÚSQUEDA ---
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                items = filtrar(query, tipoFiltro, paradas, rutas)
            },
            label = { Text("Buscar...") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(16.dp))

        // --- BOTONES DE FILTRO (usar los componentes básicos del theme) ---
        Row {
            FilterButton("Ambas", tipoFiltro == "ambas") {
                tipoFiltro = "ambas"
                items = filtrar(query, tipoFiltro, paradas, rutas)
            }
            Spacer(Modifier.width(8.dp))

            FilterButton("Paradas", tipoFiltro == "paradas") {
                tipoFiltro = "paradas"
                items = filtrar(query, tipoFiltro, paradas, rutas)
            }
            Spacer(Modifier.width(8.dp))

            FilterButton("Rutas", tipoFiltro == "rutas") {
                tipoFiltro = "rutas"
                items = filtrar(query, tipoFiltro, paradas, rutas)
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- RESULTADOS ---
        items.forEach { item ->
            when (item) {
                is ItemSearch.ParadaItem ->
                    Text(
                        text = "Parada: ${item.parada.getNombre()}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* abrir detalles si quieres */ }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                is ItemSearch.RutaItem ->
                    Text(
                        text = "Ruta: ${item.ruta.getNombre()}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* abrir detalles si quieres */ }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
            }
        }

        Spacer(Modifier.weight(1f))

        // --- BOTÓN VOLVER ---
        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Volver", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** FilterButton usa solo los componentes del theme — no cambia nada del theme global */
@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/* ---------------------------------------------------------
   FUNCIONES DE APOYO (Filtrar y combinar resultados)
   (sin cambios respecto a lo que ya tenías)
---------------------------------------------------------- */

fun buildCombined(
    paradas: List<Parada>,
    rutas: List<Ruta>
): List<ItemSearch> {
    val list = mutableListOf<ItemSearch>()
    paradas.forEach { list.add(ItemSearch.ParadaItem(it)) }
    rutas.forEach { list.add(ItemSearch.RutaItem(it)) }
    return list
}

fun filtrar(
    text: String,
    tipo: String,
    paradas: List<Parada>,
    rutas: List<Ruta>
): List<ItemSearch> {

    val t = text.trim().lowercase()

    return when (tipo) {
        // SOLO PARADAS
        "paradas" -> paradas.filter {
            it.getNombre().lowercase().contains(t) ||
                    it.getUbicacion().lowercase().contains(t)
        }.map { ItemSearch.ParadaItem(it) }

        // SOLO RUTAS
        "rutas" -> rutas.filter {
            it.getNombre().lowercase().contains(t)
        }.map { ItemSearch.RutaItem(it) }

        // AMBAS
        else -> {
            val p = paradas.filter {
                it.getNombre().lowercase().contains(t) ||
                        it.getUbicacion().lowercase().contains(t)
            }.map { ItemSearch.ParadaItem(it) }

            val r = rutas.filter {
                it.getNombre().lowercase().contains(t)
            }.map { ItemSearch.RutaItem(it) }

            p + r
        }
    }
}