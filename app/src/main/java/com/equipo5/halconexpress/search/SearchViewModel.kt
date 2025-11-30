package com.equipo5.halconexpress.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo5.halconexpress.data.DataRepo
import com.equipo5.halconexpress.data.Parada
import com.equipo5.halconexpress.data.Ruta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ItemSearch {
    data class ParadaItem(val parada: Parada) : ItemSearch()
    data class RutaItem(val ruta: Ruta) : ItemSearch()
}

class SearchViewModel : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _items = MutableStateFlow<List<ItemSearch>>(emptyList())
    val items: StateFlow<List<ItemSearch>> = _items

    private var paradasCache: List<Parada> = emptyList()
    private var rutasCache: List<Ruta> = emptyList()

    // cargar datos desde DB (llamar una vez al iniciar la pantalla)
    fun cargarDatos(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                paradasCache = DataRepo.obtenerParadas(context)
                rutasCache = DataRepo.obtenerRutas(context)
            }
            // por defecto mostramos todo al cargar
            val combined = buildCombined(paradasCache, rutasCache)
            _items.value = combined
        }
    }

    private fun buildCombined(paradas: List<Parada>, rutas: List<Ruta>): List<ItemSearch> {
        val list = mutableListOf<ItemSearch>()
        paradas.forEach { list.add(ItemSearch.ParadaItem(it)) }
        rutas.forEach { list.add(ItemSearch.RutaItem(it)) }
        return list
    }

    // tipo: "paradas", "rutas", "ambas"
    fun filtrar(text: String, tipo: String) {
        _query.value = text
        val t = text.trim().lowercase()
        val res = when (tipo) {
            "paradas" -> paradasCache.filter {
                it.getNombre().lowercase().contains(t) || it.getUbicacion().lowercase().contains(t)
            }.map { ItemSearch.ParadaItem(it) }
            "rutas" -> rutasCache.filter {
                it.getNombre().lowercase().contains(t)
            }.map { ItemSearch.RutaItem(it) }
            else -> {
                val p = paradasCache.filter {
                    it.getNombre().lowercase().contains(t) || it.getUbicacion().lowercase().contains(t)
                }.map { ItemSearch.ParadaItem(it) }
                val r = rutasCache.filter {
                    it.getNombre().lowercase().contains(t)
                }.map { ItemSearch.RutaItem(it) }
                p + r
            }
        }
        _items.value = res
    }
}