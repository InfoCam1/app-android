# Guía de Instalación y Requisitos - InfoCam

Esta guía detalla los pasos necesarios para instalar, configurar y ejecutar la aplicación móvil InfoCam, así como los requisitos técnicos mínimos.

## Requisitos del Dispositivo
Para asegurar un rendimiento óptimo y compatibilidad con todas las funciones (mapas, GPS), el dispositivo debe cumplir lo siguiente:

*   **Sistema Operativo**: Android 7.0 (Nougat) o superior.
*   **Nivel de API mínimo**: 24.
*   **Hardware**:
    *   Módulo GPS activo (Recomendado).
    *   Conexión a Internet (4G/5G o Wi-Fi).
    *   Almacenamiento: ~50MB libres.

## Permisos Requeridos
La aplicación solicitará los siguientes permisos al iniciarse:
*   **Ubicación**: Para mostrar tu posición en el mapa y localizar cámaras cercanas.
*   **Internet**: Para descargar los datos de tráfico y cámaras en tiempo real.
*   **Escritura en almacenamiento**: Necesario para el sistema de cache del mapa (osmdroid).

## Proceso de Instalación

### Opción 1: Instalación vía APK (Recomendado para usuarios)
1. Descarga el archivo `InfoCam.apk`.
2. Habilita la opción **"Instalar aplicaciones de fuentes desconocidas"** en los ajustes de seguridad de tu teléfono.
3. Abre el archivo APK y sigue las instrucciones de instalación.
4. Concede los permisos de ubicación cuando se te soliciten.

### Opción 2: Compilación desde código fuente (Para desarrolladores)
1. Abre el proyecto en **Android Studio (Hedgehog 2023.1.1 o superior)**.
2. Sincroniza el proyecto con los archivos de Gradle (`Sync Project with Gradle Files`).
3. Conecta un dispositivo físico o inicia un emulador con API 24+.
4. Pulsa el botón **"Run"** (flecha verde) o usa el comando `./gradlew assembleDebug`.

## Generación de la APK profesional
Si deseas generar una nueva versión de la APK para distribución:
1. Ve al menú **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Android Studio generará el archivo en: `app/build/outputs/apk/debug/app-debug.apk`.
3. Renombra el archivo a `InfoCam_v1.0.apk` para una mejor presentación.

---
*Esta documentación forma parte del proyecto InfoCam - 2026*
