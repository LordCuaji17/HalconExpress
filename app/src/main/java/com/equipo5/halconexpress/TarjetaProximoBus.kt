package com.equipo5.halconexpress

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ======================================================================
// TarjetaProximoBus: Encargada de toda la UI y lógica del Módulo 5
// ======================================================================

@Composable
fun TarjetaProximoBus(
    // Recibe el ID de la ruta cuyo horario queremos calcular
    idRutaParaCalculo: Int,
    // Inyección del ViewModel para el cálculo
    viewModel: DetalleParadaViewModel = viewModel()
) {
    // 1. Obtener el estado del ViewModel (se actualiza automáticamente)
    val uiState by viewModel.uiState.collectAsState()

    // 2. Ejecutar el cálculo cuando el composable se carga o cambia el ID de la ruta
    LaunchedEffect(idRutaParaCalculo) {
        viewModel.cargarProximoBus(idRutaParaCalculo)
    }

    // Estructura de la tarjeta del Módulo 5
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚌 PRÓXIMO BUS ⏳",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Mostrar el estado de carga o el mensaje final
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Calculando tiempo...")
            } else {
                // El resultado del cálculo que viene del ViewModel
                Text(
                    text = uiState.mensaje,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}