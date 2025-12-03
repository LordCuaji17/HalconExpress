# 🦅 Halcón Express - Equipo 5

Aplicación de transporte universitario para la gestión de rutas, horarios y seguimiento de autobuses.

## 🛠 Arquitectura del Proyecto

Este proyecto utiliza una arquitectura híbrida para facilitar la integración:
* **Interfaz de Usuario (UI):** Kotlin + Jetpack Compose.
* **Lógica de Datos:** Java (SQLite OpenHelper).
* **Base de Datos:** SQLite nativo.

## ⚠️ Reglas Importantes para el Equipo (LEER ANTES DE CODIFICAR)

1.  **NO modificar `HalconDataBase.java`:**
    * Este archivo es el núcleo compartido. Ya contiene la creación de tablas (`paradas`, `rutas`, `horarios`) y datos de prueba.
    * Si necesitas consultas (SELECT/INSERT), crea tu propia clase DAO (ej. `ParadasDAO.java`).
2.  **Estructura:**
    * Cada quien trabaje sus pantallas dentro de su paquete.
    * No borren recursos de otros compañeros.
3.  **Setup:**
    * Al clonar y correr la app por primera vez, la base de datos se crea sola con datos de prueba.

## 📂 Módulos Asignados

| Módulo | Descripción | Responsable |
| :--- | :--- | :--- |
| **Módulo 1** | **Mapa y Ruta:** Visualización del mapa y trazo de ruta. | **@Checo19704** |
| **Módulo 2** | **Admin Rutas:** Alta y edición de rutas y horarios. | **@LordCuaji17** |
| **Módulo 3** | **Lista Paradas:** Listado secuencial de paradas. | *Nombre Compañero* |
| **Módulo 4** | **Buscador:** Filtrado de rutas y paradas. | *Nombre Compañero* |
| **Módulo 5** | **Próximo Bus:** Cálculo de tiempo de llegada. | *Nombre Compañero* |

---
*Proyecto de la materia [Topicos Avanzados de programacion]*
