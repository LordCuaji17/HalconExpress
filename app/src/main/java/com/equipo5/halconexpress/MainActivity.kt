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
import androidx.compose.foundation.rememberScrollState // Para poder bajar si no cabe en pantalla
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // --- AQUÍ CONECTAMOS TU BD JAVA ---
        // 1. Instanciamos tu clase Java
        val dbHelper = HalconDataBase(this)

        // 2. Pedimos permiso de escritura. ESTO es lo que detona el "onCreate" de tu SQL
        // y crea el archivo "halcon_express.db" en el celular si no existe.
        val db = dbHelper.writableDatabase

        // (Opcional) Un mensajito para saber que jaló
        Toast.makeText(this, "Base de Datos Lista", Toast.LENGTH_SHORT).show()
        // ----------------------------------
        setContent {
            HalconExpressTheme {
                PantallaMenuPrincipal()
            }
        }
    }
}

@Composable
fun PantallaMenuPrincipal() {
    val colorPrimario = Color(0xFF0D1B2A)
    val colorSecundario = Color(0xFF4C96D7)
    val colorFondo = Color(0xFFF5F5F5)

    // Agregamos scroll por si en teléfonos chicos no caben los 5 botones
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
                text = "Halcón Express", // Nombre de la app
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

        // FILA 1: Mapa (M1) y Buscador (M4)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Módulo 1: Mapa (Place/Location)
            BotonMenu(
                icon = Icons.Default.Place,
                text = "MAPA Y RUTA",
                color = colorPrimario
            )
            // Módulo 4: Búsqueda (Search)
            BotonMenu(
                icon = Icons.Default.Search,
                text = "BUSCADOR",
                color = colorPrimario
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // FILA 2: Lista Paradas (M3) y Próximo Bus (M5)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Módulo 3: Lista (List)
            BotonMenu(
                icon = Icons.Default.List,
                text = "LISTA PARADAS",
                color = colorPrimario
            )
            // Módulo 5: Tiempo (Notifications/Schedule)
            BotonMenu(
                icon = Icons.Default.Notifications, // O DateRange
                text = "PRÓXIMO BUS",
                color = Color(0xFFD32F2F) // Rojo para resaltar urgencia/tiempo
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // FILA 3: Gestión Admin (M2) - Botón Ancho
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            BotonAncho(
                icon = Icons.Default.Settings,
                text = "ADMINISTRAR RUTAS Y HORARIOS",
                color = Color(0xFF455A64) // Un gris azulado para diferenciar admin
            )
        }

        Spacer(modifier = Modifier.height(30.dp)) // Espacio final
    }
}

// --- BOTÓN CUADRADO (Para las filas de 2) ---
@Composable
fun RowScope.BotonMenu(icon: ImageVector, text: String, color: Color) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .clickable { /* AQUÍ PONDREMOS LA NAVEGACIÓN LUEGO */ },
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

// --- BOTÓN ANCHO (Para el Admin) ---
@Composable
fun BotonAncho(icon: ImageVector, text: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho
            .height(80.dp)
            .clickable { /* NAVEGACIÓN ADMIN */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono a la izquierda
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(35.dp), tint = Color.White)
            }
            // Texto a la derecha
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}