package com.equipo5.halconexpress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo5.halconexpress.data.DataRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.lang.Exception // Necesario para el try/catch


fun calcularTiempoRestante(horarios: List<String>): Long? {
    val ahora = LocalTime.now()
    var proximoHorario: LocalTime? = null

    for (horarioString in horarios) {
        // Aseguramos que el string se pueda convertir a un tiempo
        val horarioSalida = try {
            LocalTime.parse(horarioString)
        } catch (e: Exception) {
            // Ignorar formatos incorrectos
            continue
        }

        // 1. Buscamos un horario que sea estrictamente DESPUÉS de ahora
        if (horarioSalida.isAfter(ahora)) {
            // 2. Encontrar el horario futuro más cercano (el que sea menor)
            if (proximoHorario == null || horarioSalida.isBefore(proximoHorario)) {
                proximoHorario = horarioSalida
            }
        }
    }

    // 3. Calcular la diferencia
    return if (proximoHorario != null) {
        ChronoUnit.MINUTES.between(ahora, proximoHorario)
    } else {
        null
    }
}

data class ProximoBusUiState(
    val isLoading: Boolean = false,
    val mensaje: String = ""
)

class DetalleParadaViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(ProximoBusUiState())
    val uiState: StateFlow<ProximoBusUiState> = _uiState

    /**
     * Inicia el cálculo del tiempo restante para la próxima salida de la ruta.
     */
    fun cargarProximoBus(idRuta: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, mensaje = "Calculando...")

        viewModelScope.launch {
            // 1. Obtener los horarios
            val horarios = DataRepo.getHorariosDeRuta(context, idRuta)

            // 2. Ejecutar la lógica de cálculo
            val minutosRestantes = calcularTiempoRestante(horarios)

            val mensajeFinal = when {
                minutosRestantes == null -> "No hay más servicio por hoy."
                minutosRestantes < 1 -> "El autobús está por llegar."
                else -> "Próximo autobús en $minutosRestantes minutos."
            }

            // 3. Actualizar el estado de la UI
            _uiState.value = ProximoBusUiState(
                isLoading = false,
                mensaje = mensajeFinal
            )
        }
    }
}