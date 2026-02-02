# Manual de Usuario: InfoCam

## Índice
1. [Introducción](#introducción)
2. [¿En qué consiste InfoCam?](#en-qué-consiste-infocam)
3. [Requisitos del Dispositivo](#requisitos-del-dispositivo)
4. [Primeros Pasos](#primeros-pasos)
    - [Descarga e Instalación](#descarga-e-instalación)
    - [Registro y Login](#registro-y-login)
    - [Configuración Inicial](#configuración-inicial)
5. [Interfaz y Navegación](#interfaz-y-navegación)
    - [Pantalla Principal (Mapa)](#pantalla-principal)
    - [Pantalla de Favoritos](#pantalla-de-favoritos)
    - [Pantalla de Usuarios (Perfil)](#pantalla-de-usuarios)
    - [Iconografía (Diccionario Visual)](#iconografía)
6. [Guía de Funcionalidades](#guía-de-funcionalidades)
    - [Crear una incidencia](#crear-una-incidencia)
    - [Marcar una cámara como favorita](#marcar-una-cámara-como-favorita)
7. [Configuración y Privacidad](#configuración-y-privacidad)
8. [Solución de Problemas (FAQ)](#solución-de-problemas-faq)
9. [Contacto](#contacto)

---

## Introducción
Bienvenido a **InfoCam**, tu compañero inteligente en la carretera. Este manual ha sido diseñado para ayudarte a aprovechar al máximo todas las herramientas que nuestra aplicación pone a tu disposición para que tus trayectos sean más seguros y predecibles.

## ¿En qué consiste InfoCam?
InfoCam es una plataforma colaborativa y de consulta que centraliza la información del tráfico. A través de la aplicación, puedes ver qué está ocurriendo en las carreteras en tiempo real gracias a las cámaras de tráfico oficiales y a los reportes de otros conductores. Nuestra misión es que nunca te encuentres con una sorpresa inesperada en tu ruta.

## Requisitos del Dispositivo
Para asegurar un funcionamiento fluido de la aplicación, tu dispositivo debe cumplir con lo siguiente:
- **Sistema Operativo:** Android 7.0 (Nougat) o superior.
- **Conexión a Internet:** Necesaria para descargar datos de la API y visualizar las imágenes de las cámaras.
- **Permisos de Ubicación:** Recomendados para centrar el mapa en tu posición actual y facilitar el reporte de incidencias cercanas.
- **Espacio en memoria:** Aproximadamente 50MB para la instalación y datos en caché.

---

## Primeros Pasos

### Descarga e Instalación
Actualmente, la aplicación se distribuye en formato **APK**. Para instalarla:
1. Descarga el archivo `infocam.apk` proporcionado por el administrador.
2. Abre el archivo en tu dispositivo Android.
3. Si el sistema lo solicita, autoriza la "Instalación desde fuentes desconocidas".
4. Pulsa en "Instalar" y espera a que el icono de InfoCam aparezca en tu menú de aplicaciones.

### Registro y Login
Al abrir la app por primera vez, verás la pantalla de **Acceso**:
- **Si ya tienes cuenta:** Introduce tu nombre de usuario y contraseña.
- **Si eres nuevo:** Pulsa en "Registrarse". Deberás indicar tu nombre, correo electrónico, teléfono y elegir una contraseña segura. 
> [!TIP]
> Tu contraseña es cifrada inmediatamente para tu seguridad; nadie, ni siquiera los administradores, pueden verla en texto plano.

### Configuración Inicial
Una vez dentro, la aplicación te pedirá permiso para acceder a tu ubicación. Te recomendamos aceptarlo para que el mapa se sitúe automáticamente sobre tu posición, permitiéndote ver de inmediato el tráfico que te rodea.

---

## Interfaz y Navegación

La navegación es sencilla e intuitiva gracias a la barra inferior:

### Pantalla Principal (Mapa)
Es el corazón de la app. Desde aquí puedes:
- Moverte libremente por el mapa deslizando el dedo.
- Hacer zoom (pellizcando la pantalla) para ver áreas específicas.
- Pulsar sobre cualquier **icono** para ver detalles.

### Pantalla de Favoritos
Aquí se guardan las cámaras que usas con más frecuencia. Es ideal para comprobar rápidamente el estado del tráfico en tu ruta diaria al trabajo o de vuelta a casa sin tener que buscarlas en el mapa.

### Pantalla de Usuarios (Perfil)
Accede a tus datos personales. Puedes ver tu nombre, email y teléfono asociados a la cuenta. También encontrarás aquí el botón para **Cerrar Sesión** de forma segura.

### Iconografía (Pequeño diccionario)
Para que no te pierdas nada, aquí tienes el significado de los iconos que verás en el mapa:

| Icono | Significado |
| :---: | :--- |
| ![Cámara](/app/src/main/res/drawable/ic_marker_camera.xml) | **Cámara de Tráfico:** Pulsa para ver la imagen en tiempo real. |
| ![Incidencia Oficial](/app/src/main/res/drawable/ic_marker_incident.xml) | **Incidencia Oficial:** Reportes automáticos de la DGT/Gobierno (obras, retenciones). |
| ![Incidencia Usuario](/app/src/main/res/drawable/ic_marker_incident_user.xml) | **Incidencia Ciudadana:** Reportada por un conductor como tú. |
| ![Estrella Dorada](/app/src/main/res/drawable/ic_star_filled.xml) | **Favorito:** Indica que esa cámara está en tu lista preferida. |

---

## Guía de Funcionalidades

### Crear una incidencia
¿Has visto un accidente o una retención que no aparece en el mapa? ¡Ayuda a otros!
1. En la pantalla del Mapa, pulsa el **botón rojo flotante (+)**.
2. Se abrirá un formulario.
3. Selecciona el tipo de incidencia (Accidente, Obras, Retención, etc.).
4. Elige la fecha y hora aproximada.
5. Escribe una breve descripción si lo deseas.
6. Pulsa en "Enviar". ¡Tu reporte aparecerá al instante para todos los usuarios!

### Marcar una cámara como favorita
1. Pulsa sobre cualquier icono de cámara en el mapa.
2. Se abrirá un pequeño cuadro informativo.
3. Verás un icono de una **estrella**.
4. Púlsa la estrella. Si se rellena, la cámara ya está en tu lista de Favoritos.

---

## Configuración y Privacidad

### Declaración de Privacidad
En InfoCam nos tomamos muy en serio tu privacidad:
- **Tus datos están seguros:** Tu información personal no se comparte con terceros.
- **Ubicación:** Tu ubicación solo se usa para centrar el mapa y geolocalizar las incidencias que decidas reportar. No rastreamos tus movimientos.
- **Transparencia:** Puedes consultar qué datos tenemos de ti en cualquier momento desde la pantalla de Perfil.

---

## Solución de Problemas (FAQ)

### No veo ninguna incidencia, ¿qué debo hacer?
Asegúrate de tener conexión a internet activa. Si el problema persiste, revisa el **botón de filtros** en el mapa; es posible que tengas desactivada la visualización de incidencias por error.

### ¿Cómo puedo cerrar sesión?
Ve a la pantalla de **Perfil** (el icono de la persona abajo a la derecha) y pulsa el botón rojo de "Cerrar Sesión". Esto borrará tus credenciales temporales del dispositivo por seguridad.

### La app se cierra inesperadamente, ¿qué puedo hacer?
1. Intenta limpiar la caché de la aplicación desde los ajustes de tu teléfono.
2. Asegúrate de tener la última versión disponible del APK.
3. Si el error continúa, contacta con nosotros indicando tu modelo de teléfono.

---

## Contacto
Si tienes dudas adicionales, sugerencias o quieres reportar un error técnico:
- **Email de soporte:** soporte@infocam.es
- **Web del proyecto:** [www.infocam.es](http://www.infocam.es)
