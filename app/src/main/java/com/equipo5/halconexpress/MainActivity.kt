package com.equipo5.halconexpress

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // <--- IMPORTANTE: Para recordar estados
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo5.halconexpress.data.HalconDataBase
import com.equipo5.halconexpress.ui.theme.HalconExpressTheme
import androidx.compose.foundation.background


//MODULO 3 TERMINADO - LISTA DE PARADAS CON DETALLE - FGS


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- BASE DE DATOS ---
        val dbHelper = HalconDataBase(this)
        dbHelper.readableDatabase // Fuerza la creación si no existe
        Toast.makeText(this, "Sistema Listo", Toast.LENGTH_SHORT).show()
        // ---------------------

        setContent {
            HalconExpressTheme {
                // --- CONTROL DE NAVEGACIÓN ---
                // Variable que recuerda en qué pantalla estamos. Inicia en "menu"
                var pantallaActual by remember { mutableStateOf("menu") }

                // En el setContent del MainActivity, actualiza el when:
                when (pantallaActual) {
                    "menu" -> {
                        PantallaMenuPrincipal(
                            onNavegarAdmin = { pantallaActual = "admin" },
                            onNavegarParadas = { pantallaActual = "paradas" } // <-- Agregar esto
                        )
                    }
                    "admin" -> {
                        PantallaAdminRutas(
                            onVolver = { pantallaActual = "menu" }
                        )
                    }
                    "paradas" -> {
                        PantallaListaParadas(
                            onVolver = { pantallaActual = "menu" }
                        )
                    }
                    "buscador" -> {
                        PantallaBuscador(
                            context = this,
                            onVolver = { pantallaActual = "menu" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaMenuPrincipal(
    onNavegarAdmin: () -> Unit,
    onNavegarParadas: () -> Unit  // <-- AGREGAR ESTE PARÁMETRO
) {
    val colorPrimario = Color(0xFF0D1B2A)
    val colorSecundario = Color(0xFF4C96D7)
    val colorFondo = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. ENCABEZADO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorPrimario)
                .padding(top = 40.dp, bottom = 15.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Halcón Express",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- 2. PERFIL ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(colorSecundario)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.halcon),
                    contentDescription = "Foto Perfil",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ID: XXXXXXX", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "Nombre",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimario
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // --- 3. MÓDULOS DEL PROYECTO ---

        // FILA 1
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            BotonMenu(Icons.Default.Place, "MAPA Y RUTA", colorPrimario)
            BotonMenu(Icons.Default.Search, "BUSCADOR", colorPrimario)
        }

        Spacer(modifier = Modifier.height(15.dp))

        // FILA 2 - ACTUALIZAR BOTÓN LISTA PARADAS
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            BotonMenu(
                Icons.Default.List,
                "LISTA PARADAS",
                colorPrimario,
                onClick = onNavegarParadas  // <-- CONECTAR AQUÍ
            )
            BotonMenu(Icons.Default.Notifications, "PRÓXIMO BUS", Color(0xFFD32F2F))
        }

        Spacer(modifier = Modifier.height(15.dp))

        // FILA 3: Gestión Admin (CONECTADA)
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            BotonAncho(
                icon = Icons.Default.Settings,
                text = "ADMINISTRAR RUTAS Y HORARIOS",
                color = Color(0xFF455A64),
                onClick = onNavegarAdmin
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}




// --- BOTÓN CUADRADO ACTUALIZADO ---
@Composable
fun RowScope.BotonMenu(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit = {} // <-- Agregar este parámetro con valor por defecto
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .clickable { onClick() }, // <-- Usar el parámetro aquí
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(45.dp), tint = color)
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(color).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- BOTÓN ANCHO (ADMIN) ---
@Composable
// Agregamos el parámetro onClick para recibir el clic
fun BotonAncho(icon: ImageVector, text: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }, // <--- Ejecutamos la acción al tocar
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(35.dp), tint = Color.White)
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}