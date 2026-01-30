# 📔 Explicación Técnica Exhaustiva: InfoCam

Este documento detalla cada uno de los componentes del proyecto, organizados por su función en la arquitectura del sistema.

---

## 🖥️ Backend: API Java (Spring Boot)
El servidor gestiona la persistencia en MySQL y la sincronización con fuentes externas.

### 📂 Lógica y Servicios
*   **`SyncController` & `SyncService`**: Gestionan la importación masiva. `SyncService` parsea el JSON de la API de Euskadi y usa **`CoordinateConverter`** para transformar coordenadas UTM a WGS84.
*   **`IncidenciaController` & `IncidenciaService`**: Endpoints para leer y crear incidencias. `IncidenciaService` incluye filtros temporales para evitar saturar el mapa con datos antiguos.
*   **`CamaraController` & `CamaraService`**: Gestionan el catálogo de cámaras de tráfico.
*   **`UsuarioController`**: Maneja el CRUD de usuarios y la actualización de perfiles.
*   **`AuthController`**: Implementa el registro y login, devolviendo el Token necesario para las peticiones seguras.

---

## 📱 Frontend: Aplicación Android

### 📂 Modelos de Datos (`com.infocam.model`)
*   **`Usuario`**: POJO con los datos del perfil (nombre, email, teléfono, admin, etc.).
*   **`Camara`**: Datos de latitud, longitud y la URL de la imagen en tiempo real.
*   **`Incidencia`**: Define si el reporte es oficial o de usuario, causa, fechas y coordenadas.
*   **`Favorito`**: Clase específica para los elementos guardados localmente por el usuario.

### 📂 Capa de Datos y Persistencia (`com.infocam.data`)
*   **`DatabaseHelper`**: Configuración de **SQLite**. Crea y mantiene la tabla de favoritos local.
*   **`DataRepository`**: Capa de abstracción. Realiza las consultas SQL y sincroniza con el servidor.
*   **`SessionManager`**: Gestiona **SharedPreferences**. Guarda el estado de la sesión, el token y los datos del usuario logueado.

### 📂 Capa de Red (`com.infocam.network`)
*   **`ServicioApi`**: Corazón de la comunicación. Usa el patrón **Singleton**. Implementa la comunicación mediante **Retrofit**, gestionando peticiones asíncronas de forma profesional.
*   **`ApiCallback<T>`**: Interfaz genérica que estandariza las respuestas de éxito y error de todas las peticiones.

### 📂 Pantallas y Componentes UI (`com.infocam.ui`)
*   **`LoginActivity`**: Control de acceso. Valida credenciales e inicia la descarga de favoritos.
*   **`RegisterActivity`**: Formulario de alta con validación de campos y creación de objeto `Usuario`.
*   **`MainActivity`**: Contenedor principal. Implementa el **BottomNavigationView** y alterna entre los Fragmentos.
*   **`MapaFragment`**: El componente más complejo. Usa **OSMDroid**. Gestiona filtros, capas de marcadores, pulsaciones largas y el radar de posición del usuario.
*   **`CrearIncidenciaActivity`**: Formulario de reporte. Usa `DatePickerDialog` y `TimePickerDialog` para capturar fechas precisas.
*   **`FavoritosFragment`**: Pantalla de gestión. Lista las cámaras guardadas y permite eliminarlas sincronizando con la API.
*   **`PerfilFragment`**: Vista del usuario. Permite editar datos personales y realizar el cierre de sesión seguro.
*   **`FullScreenImageActivity`**: Abre la imagen de la cámara a pantalla completa usando **PhotoView** para permitir gestos de zoom (pinch-to-zoom).
*   **`AdaptadorFavoritos`**: El puente entre la lista de Java y el **RecyclerView**, utilizando el patrón **ViewHolder** para un scroll fluido.

---

## 🎨 Arquitectura de Diseño (Layouts XML)

### 📂 Actividades e Interfaces
*   **`activity_login.xml`**: Login minimalista con fondos oscuros y campos redondeados.
*   **`activity_register.xml`**: Layout con `ScrollView` que permite rellenar todos los campos de registro cómodamente.
*   **`activity_main.xml`**: Define el `FragmentContainerView` y el menú de navegación inferior.
*   **`activity_crear_incidencia.xml`**: Formulario elegante con etiquetas claras para el reporte ciudadano.
*   **`activity_full_screen_image.xml`**: Contenedor para el visor `PhotoView` con un botón de cierre flotante.

### 📂 Fragmentos y Listados
*   **`fragment_map.xml`**: Contiene el mapa, controles de zoom, el botón de filtros y el botón rojo de ayuda (FAB).
*   **`fragment_favoritos.xml`**: Estructura simple de encabezado y un `RecyclerView` para la lista de cámaras.
*   **`fragment_perfil.xml`**: Layout con estilo de configuración de sistema, agrupando el avatar y los campos de edición.
*   **`item_favorito.xml`**: El diseño de cada "tarjeta" en la lista de favoritos. Usa `CardView` y `ShapeableImageView`.
*   **`info_window_camera.xml`**: Burbuja de información para cámaras con botón de estrella para favoritos.
*   **`info_window_incident.xml`**: Ventana dedicada para incidencias, mostrando tipo, causa y fechas.
*   **`spinner_item.xml`**: Define el aspecto visual de cada elemento en los desplegables (como el selector de tipo de incidencia), asegurando coherencia visual con el modo oscuro.

---

## 🔧 Configuración del Sistema
*   **`build.gradle` (Project/App)**: Configuran el SDK 34, las dependencias (Glide, Proj4J, PhotoView) y la gestión del ciclo de vida del build.
*   **`settings.gradle`**: Configura los repositorios de descarga de librerías (Google, Maven Central, Jitpack).
