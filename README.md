# 🚦 InfoCam - Gestión de Tráfico del País Vasco

**InfoCam** es una plataforma integral para la monitorización de tráfico en tiempo real, diseñada específicamente para el entorno de Euskadi. El sistema combina una aplicación Android intuitiva con una potente API en Java para ofrecer datos actualizados sobre cámaras de tráfico, incidencias viales y reportes ciudadanos.

---

## ✨ Características Principales

*   **🗺️ Mapa Interactivo**: Visualización completa del estado del tráfico utilizando **OpenStreetMap (osmdroid)**.
*   **📷 Red de Cámaras**: Acceso a imágenes en tiempo real de las cámaras de Trafikoa (Euskadi), Bizkaia y Bilbao.
*   **⚠️ Gestión de Incidencias**:
    *   Sincronización automática con la API oficial de Euskadi.
    *   Posibilidad de que los usuarios reporten nuevas incidencias (Crowdsourcing).
*   **🧭 Conversión Inteligente**: Motor de conversión de coordenadas **UTM (ETRS89) a WGS84** integrado en el backend para garantizar precisión absoluta en el mapa.
*   **🌟 Favoritos**: Guarda tus cámaras más consultadas para un acceso rápido.
*   **👤 Perfil de Usuario**: Gestión de sesiones y personalización de la experiencia.

---

## 🛠️ Stack Tecnológico

### Frontend (Android)
*   **Lenguaje**: Java 17+
*   **Arquitectura**: MVC / Fragmentos.
*   **Librerías Clave**:
    *   `osmdroid`: Visualización de mapas offline/online.
    *   `Glide`: Carga eficiente de imágenes de cámaras.
    *   `Material Design 3`: Interfaz moderna y adaptable.
    *   `Retrofit`: Comunicación avanzada y eficiente con la API REST (ahora implementado).

### Backend (Java API)
*   **Framework**: Spring Boot 3.x
*   **Base de Datos**: MySQL
*   **Librerías Clave**:
    *   `Proj4J`: Transformación de sistemas de referencia geográficos.
    *   `Spring Data JPA`: Persistencia de datos simplificada.

---

## 📸 Previsualización

*   **Mapa Principal**: Con filtros dinámicos y localización precisa.
*   **Reporte de Incidencias**: Interfaz guiada mediante botones flotantes inteligentes.

---

## 🚀 Instalación
Para instrucciones detalladas sobre cómo compilar y ejecutar el proyecto, consulta la guía de instalación:
👉 **[Guía de Instalación (INSTALL.md)](INSTALL.md)**

---

## 📄 Créditos y Desarrollo
Este proyecto ha sido desarrollado como parte del segundo reto de segundo curso del ciclo formativo de **Desarrollo de Aplicaciones Multiplataforma (DAM)**.

Desarrollado por [InfoCam] - 2026.