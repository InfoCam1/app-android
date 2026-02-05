package com.infocam.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.infocam.R;
import com.infocam.data.DataRepository;
import com.infocam.data.SessionManager;
import com.infocam.model.Camara;
import com.infocam.model.Favorito;
import com.infocam.model.Incidencia;
import com.infocam.model.Usuario;
import com.infocam.network.ApiCallback;
import com.infocam.network.InfocamServiceClient;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Esta clase gestiona la visualización del mapa, cámaras de tráfico e incidencias.
public class MapaFragment extends Fragment {
    // Componentes del mapa.
    private MapView visorMapa;
    private MyLocationNewOverlay capaPosicionUsuario; // Capa que muestra dónde se encuentra el usuario.

    // Datos de sesión y base de datos.
    private SessionManager preferenciaSesion;
    private DataRepository databaseLocal;
    private List<Camara> listaFavoritosApi = new ArrayList<>();
    private List<Camara> listaCamarasApi = new ArrayList<>();
    private List<Incidencia> listaIncidenciasApi = new ArrayList<>();

    // Variables de control de filtros.
    private View panelFiltros;
    private CheckBox checkCamaras, checkIncidenciasG, checkIncidenciasU, checkSoloFavoritos;
    private boolean verCamaras = true, verIncidenciasG = true, verIncidenciasU = true, verSoloFavs = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoAnterior) {
        // Configuración obligatoria de Osmdroid. Carga el caché de baldosas (el mapa se
        // va cargando como si fueran baldosas en un suelo).
        Configuration.getInstance().load(getContext(),
                getContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));

        View vista = inflador.inflate(R.layout.fragment_map, contenedor, false);

        // Iniciamos la vista del mapa.
        visorMapa = vista.findViewById(R.id.map);
        visorMapa.setTileSource(TileSourceFactory.MAPNIK); // Estilo de mapa estándar.
        visorMapa.setMultiTouchControls(true); // Permite controlar el mapa con gestos.
        visorMapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER); // Eliminamos botones
                                                                                                   // de zoom nativos
                                                                                                   // para añadir
                                                                                                   // posteriormente los
                                                                                                   // nuestros
                                                                                                   // personalizados.

        preferenciaSesion = new SessionManager(getContext());
        databaseLocal = new DataRepository(getContext());

        // Nuestros botones de zoom.
        vista.findViewById(R.id.btnZoomIn).setOnClickListener(v -> visorMapa.getController().zoomIn());
        vista.findViewById(R.id.btnZoomOut).setOnClickListener(v -> visorMapa.getController().zoomOut());

        // Colocamos Irun como centro inicial.
        GeoPoint puntoInicio = obtenerUltimaPosicionConocida();
        if (puntoInicio == null)
            puntoInicio = new GeoPoint(43.3186, -1.7737);

        visorMapa.getController().setZoom(14.0);
        visorMapa.getController().setCenter(puntoInicio);

        // Comprobamos que los permisos de GPS estén activados.
        verificarPermisosGps();
        configurarMenuFiltros(vista);

        // Lógica del botón que centra la ubicación del usuario en el mapa.
        vista.findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (capaPosicionUsuario != null && capaPosicionUsuario.getMyLocation() != null) {
                visorMapa.getController().animateTo(capaPosicionUsuario.getMyLocation());
                visorMapa.getController().setZoom(14.0);
            } else {
                Toast.makeText(getContext(), "Buscando señal GPS...", Toast.LENGTH_SHORT).show();
                verificarPermisosGps(); // Reintenta activar si no estaba activa.
            }
        });

        // Lógica del botón que muestra una explicación sobre cómo reportar una
        // incidencia.
        vista.findViewById(R.id.fabReportar).setOnClickListener(v -> {
            new AlertDialog.Builder(getContext(), R.style.Theme_InfoCam_Dialog)
                    .setTitle("Cómo crear una incidencia")
                    .setMessage(
                            "Para reportar una nueva incidencia, mantén pulsado durante 1 segundo el punto exacto del mapa donde ha ocurrido.")
                    .setPositiveButton("Entendido", null)
                    .show();
        });

        return vista;
    }

    // Gestión del menú de filtros.
    private void configurarMenuFiltros(View raiz) {
        // Vinculaciones con los componentes.
        panelFiltros = raiz.findViewById(R.id.panelFiltros);
        checkCamaras = raiz.findViewById(R.id.cbFiltroCamaras);
        checkIncidenciasG = raiz.findViewById(R.id.cbFiltroIncidenciasGov);
        checkIncidenciasU = raiz.findViewById(R.id.cbFiltroIncidenciasUser);
        checkSoloFavoritos = raiz.findViewById(R.id.cbFiltroFavoritos);

        // Cuando volvemos a hacer click en el botón, se muestra u oculta el panel.
        raiz.findViewById(R.id.btnMenuFiltros).setOnClickListener(v -> {
            panelFiltros.setVisibility(panelFiltros.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        raiz.findViewById(R.id.btnCerrarFiltros).setOnClickListener(v -> {
            // Sincronizamos el estado de las variables con los checkbox.
            verCamaras = checkCamaras.isChecked();
            verIncidenciasG = checkIncidenciasG.isChecked();
            verIncidenciasU = checkIncidenciasU.isChecked();
            verSoloFavs = checkSoloFavoritos.isChecked();

            panelFiltros.setVisibility(View.GONE); // Ocultamos el panel.
            refrescarMarcadores(); // Volvemos a dibujar el mapa con los marcadores seleccionados.
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        visorMapa.onResume();
        if (capaPosicionUsuario != null)
            capaPosicionUsuario.enableMyLocation();
        repintarElementosEnMapa();
    }

    @Override
    public void onPause() {
        super.onPause();
        visorMapa.onPause();
        if (capaPosicionUsuario != null)
            capaPosicionUsuario.disableMyLocation();
    }

    private void verificarPermisosGps() {
        // Si no se ha concedido el permiso, se solicita.
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    101);
        } else {
            activarCapaPosicion(); // Si se ha concedido el permiso, se activa la capa de posición.
        }
    }

    private void activarCapaPosicion() {
        capaPosicionUsuario = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), visorMapa);
        capaPosicionUsuario.enableMyLocation();

        capaPosicionUsuario.setDrawAccuracyEnabled(false);
        // Usamos solo el icono de persona para mayor claridad. Desechamos la flecha
        // blanca que viene por defecto en este mapa.
        Bitmap bUser = drawableToBitmap(ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_user)); // Convertimos
                                                                                                             // el icono
                                                                                                             // a
                                                                                                             // Bitmap.
        if (bUser != null) {
            capaPosicionUsuario.setPersonIcon(bUser);
            capaPosicionUsuario.setDirectionIcon(bUser); // Usamos el icono de persona para la flecha de dirección.
        }

        // Movemos el mapa a la ubicación del usuario.
        capaPosicionUsuario.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    GeoPoint loc = capaPosicionUsuario.getMyLocation();
                    if (loc != null) {
                        visorMapa.getController().animateTo(loc);
                        // Forzamos zoom cercano si estamos en el centro por defecto
                        if (visorMapa.getZoomLevelDouble() < 10) {
                            visorMapa.getController().setZoom(16.0);
                        }
                    }
                });
            }
        });
        visorMapa.getOverlays().add(capaPosicionUsuario); // Añadimos la capa al mapa.
    }

    private void repintarElementosEnMapa() {
        visorMapa.getOverlays().clear(); // Limpiamos los overlays anteriores.

        // Overlay para detectar pulsaciones largas y crear incidencias.
        MapEventsOverlay capaEventos = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            // Haciendo click una sola vez, se cierran todas las ventanas.
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(visorMapa);
                return false;
            }

            @Override
            // Haciendo un click largo, se abre el formulario de creación de incidencias.
            public boolean longPressHelper(GeoPoint p) {
                lanzarNuevaIncidencia(p);
                return true;
            }
        });
        visorMapa.getOverlays().add(capaEventos);

        if (capaPosicionUsuario != null)
            visorMapa.getOverlays().add(capaPosicionUsuario);

        // Cargamos favoritos locales primero para que se vean mientras descarga la API.
        Usuario u = preferenciaSesion.obtenerUsuario();
        if (u != null) {
            List<Favorito> localFavs = databaseLocal.obtenerFavoritosPorUsuario(u.getId());
            List<Camara> camFavs = new ArrayList<>();
            for (Favorito f : localFavs) {
                Camara c = new Camara();
                c.setId(f.getIdCamara());
                c.setNombre(f.getNombre());
                c.setLatitud(f.getLatitud());
                c.setLongitud(f.getLongitud());
                c.setImagen(f.getUrlImagen());
                camFavs.add(c);
            }
            listaFavoritosApi = camFavs;
        }

        refrescarMarcadores(); // Refrescamos los marcadores.
        sincronizarYDescargar(); // Sincronizamos y descargamos.
    }

    // Limpiamos el mapa y volvemos a dibujar los marcadores.
    private void refrescarMarcadores() {
        // Guardamos los overlays que no queremos borrar (el de pulsación larga y
        // posición usuario)
        List<org.osmdroid.views.overlay.Overlay> overlaysFijos = new ArrayList<>();
        for (org.osmdroid.views.overlay.Overlay o : visorMapa.getOverlays()) {
            if (o instanceof MapEventsOverlay || o instanceof MyLocationNewOverlay) {
                overlaysFijos.add(o);
            }
        }

        visorMapa.getOverlays().clear();
        visorMapa.getOverlays().addAll(overlaysFijos);

        // Identificamos qué cámaras son favoritas para ponerles el icono de favorito.
        java.util.Set<Integer> setIdsFavs = new java.util.HashSet<>();
        for (Camara f : listaFavoritosApi) {
            setIdsFavs.add(f.getId());
        }
        // Dibujamos según lo que el usuario haya filtrado.
        // Caso 1: Solo queremos ver favoritos.
        if (verSoloFavs) {
            for (Camara f : listaFavoritosApi) {
                if (verCamaras) {
                    dibujarMarcadorCamara(f, true);
                }
            }
        }
        // Caso 2: Ver todo según los filtros.
        else {
            if (verCamaras) {
                for (Camara c : listaCamarasApi) {
                    boolean esFavorita = setIdsFavs.contains(c.getId());
                    dibujarMarcadorCamara(c, esFavorita);
                }
            }

            // Pintamos incidencias según los filtros, teniendo en cuenta si son incidencias
            // de usuarios u oficiales.
            for (Incidencia i : listaIncidenciasApi) {
                if (i.isOficial() && !verIncidenciasG)
                    continue;
                if (!i.isOficial() && !verIncidenciasU)
                    continue;

                dibujarMarcadorIncidencia(i);
            }
        }

        visorMapa.invalidate(); // Forzamos un refresco del mapa.
    }

    // Dibuja un marcador de incidencia en el mapa.
    private void dibujarMarcadorIncidencia(Incidencia i) {
        Marker m = new Marker(visorMapa);
        m.setPosition(new GeoPoint(i.getLatitud(), i.getLongitud()));
        m.setTitle(i.getNombre());

        int resIcono = i.isOficial() ? R.drawable.ic_marker_incident : R.drawable.ic_marker_incident_user; // Icono
                                                                                                           // según si
                                                                                                           // es oficial
                                                                                                           // o no.
        m.setIcon(ContextCompat.getDrawable(getContext(), resIcono));

        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Posición del icono.
        m.setInfoWindow(new VentanaDetalleIncidencia(visorMapa, i)); // Ventana de detalle.
        visorMapa.getOverlays().add(m);
    }

    // Sincroniza y descarga los datos del servidor.
    private void sincronizarYDescargar() {
        Usuario u = preferenciaSesion.obtenerUsuario();

        // Lanzamos las peticiones en paralelo para no bloquearnos.
        traerCamarasServidor();
        traerIncidenciasServidor();

        // Sincronizamos y descargamos los favoritos.
        if (u != null) { // Si no hay usuario, no hay favoritos.
            InfocamServiceClient.obtenerInstancia().obtenerFavoritosUsuario(preferenciaSesion.getToken(), u.getId(),
                    new ApiCallback<List<Camara>>() {
                        @Override
                        public void onSuccess(List<Camara> result) {
                            if (getContext() == null)
                                return;
                            listaFavoritosApi = result;
                            databaseLocal.sincronizarConServidor(u.getId(), result);
                            refrescarMarcadores();
                        }

                        @Override
                        public void onError(String error) {
                            // Si falla, al menos tenemos los locales cargados previamente
                            if (getContext() == null)
                                return;
                            refrescarMarcadores();
                        }
                    });
        }
    }

    // Trae las cámaras activas del servidor.
    private void traerCamarasServidor() {
        InfocamServiceClient.obtenerInstancia().obtenerCamarasActivas(preferenciaSesion.getToken(),
                new ApiCallback<List<Camara>>() {
                    @Override
                    public void onSuccess(List<Camara> result) {
                        if (getContext() == null)
                            return;
                        listaCamarasApi = result;
                        refrescarMarcadores();
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    // Dibuja un marcador de cámara en el mapa.
    private void dibujarMarcadorCamara(Camara c, boolean esFavorita) {
        Marker m = new Marker(visorMapa);
        m.setPosition(new GeoPoint(c.getLatitud(), c.getLongitud()));
        m.setIcon(ContextCompat.getDrawable(getContext(),
                esFavorita ? R.drawable.ic_marker_favorite : R.drawable.ic_marker_camera));
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setInfoWindow(new VentanaDetalleCamara(visorMapa, c, esFavorita));
        visorMapa.getOverlays().add(m);
    }

    // Trae TODAS las incidencias del servidor.
    private void traerIncidenciasServidor() {
        Usuario u = preferenciaSesion.obtenerUsuario();
        Integer idU = (u != null) ? u.getId() : null;

        InfocamServiceClient.obtenerInstancia().obtenerIncidencias(preferenciaSesion.getToken(), idU,
                new ApiCallback<List<Incidencia>>() {
                    @Override
                    public void onSuccess(List<Incidencia> result) {
                        if (getContext() == null)
                            return;
                        listaIncidenciasApi = result;
                        refrescarMarcadores();
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    // Lanza el diálogo para crear una nueva incidencia.
    private void lanzarNuevaIncidencia(GeoPoint p) {
        new AlertDialog.Builder(getContext(), R.style.Theme_InfoCam_Dialog)
                .setTitle("Reportar Incidencia")
                .setMessage("¿Deseas informar de un incidente en este punto?")
                .setPositiveButton("Crear", (d, w) -> {
                    Intent i = new Intent(getContext(), CrearIncidenciaActivity.class);
                    i.putExtra("latitud", p.getLatitude());
                    i.putExtra("longitud", p.getLongitude());
                    startActivity(i);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Convierte un drawable a bitmap. Esto es necesario para que el marcador se
    // dibuje correctamente.
    private Bitmap drawableToBitmap(Drawable d) {
        if (d == null)
            return null;
        Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return b;
    }

    // Obtiene la última posición conocida del usuario.
    private GeoPoint obtenerUltimaPosicionConocida() {
        if (getContext() == null || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) // Si no se tiene
                                                                                                // permiso, no se puede
                                                                                                // obtener la última
                                                                                                // posición.
            return null;
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE); // Obtenemos el
                                                                                                        // servicio de
                                                                                                        // localización.
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); // Obtenemos la última posición conocida.
        if (loc == null)
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // Si no se tiene, obtenemos la última
                                                                             // posición conocida.
        return (loc != null) ? new GeoPoint(loc.getLatitude(), loc.getLongitude()) : null; // Si no se tiene, devolvemos
                                                                                           // null.
    }

    // Ventana emergente personalizada para mostrar la información de una cámara.
    private class VentanaDetalleCamara extends InfoWindow {
        private Camara cam; // Cámara a la que pertenece la ventana.
        private boolean esFav; // Indica si la cámara es favorita.

        public VentanaDetalleCamara(MapView mv, Camara cam, boolean esFav) {
            super(R.layout.info_window_camera, mv); // Inicializamos la ventana con el layout.
            this.cam = cam; // Asignamos la cámara.
            this.esFav = esFav; // Asignamos si la cámara es favorita.
        }

        @Override
        public void onOpen(Object item) { // Al abrir la ventana, se actualizan los valores.
            InfoWindow.closeAllInfoWindowsOn(visorMapa); // Cerramos todas las ventanas emergentes.
            View v = getView(); // Obtenemos la vista de la ventana.
            ((TextView) v.findViewById(R.id.bubble_title)).setText(cam.getNombre()); // Asignamos el nombre de la
                                                                                     // cámara.
            ImageView img = v.findViewById(R.id.bubble_image);
            ImageButton btn = v.findViewById(R.id.bubble_favorite);

            btn.setImageResource(esFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

            // Añadimos "cache-buster" para forzar actualización de la imagen en tiempo real
            String cacheBusterUrl = cam.getImagen() + (cam.getImagen().contains("?") ? "&" : "?") + "t="
                    + System.currentTimeMillis();

            // Cargamos la imagen de la cámara.
            Glide.with(getContext())
                    .load(cacheBusterUrl) // Cargamos la imagen.
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // No se cachea la imagen.
                    .skipMemoryCache(true) // No se cachea la imagen en memoria.
                    .placeholder(R.drawable.ic_marker_camera) // Placeholder de la imagen.
                    .into(img); // Cargamos la imagen.

            // Al hacer clic en la imagen, se abre la imagen en pantalla completa.
            img.setOnClickListener(c -> {
                Intent i = new Intent(getContext(), FullScreenImageActivity.class); // Creamos el intent.
                i.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, cam.getImagen()); // Asignamos la URL de la imagen.
                startActivity(i); // Iniciamos la actividad.
            });

            btn.setOnClickListener(c -> accionarFavorito(btn, (Marker) item));
        }

        // Acciona el toggle de favorito.
        private void accionarFavorito(ImageButton btn, Marker m) {
            Usuario u = preferenciaSesion.obtenerUsuario();
            if (u == null) // Si no se tiene usuario, no se puede accionar el favorito.
                return;

            InfocamServiceClient.obtenerInstancia().conmutarFavorito(preferenciaSesion.getToken(), cam.getId(),
                    u.getId(),
                    new ApiCallback<Void>() {
                        // Al obtener el resultado, se actualiza el estado del favorito.
                        @Override
                        public void onSuccess(Void result) {
                            esFav = !esFav;
                            btn.setImageResource(esFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
                            m.setIcon(ContextCompat.getDrawable(getContext(),
                                    esFav ? R.drawable.ic_marker_favorite : R.drawable.ic_marker_camera));

                            // Actualizamos la lista de favoritos local para que el filtro responda.
                            if (esFav) {
                                listaFavoritosApi.add(cam);
                                Favorito fav = new Favorito(u.getId(), cam.getId(), cam.getNombre(),
                                        "Cámara de tráfico", cam.getLatitud(), cam.getLongitud(), cam.getImagen()); // Creamos
                                                                                                                    // el
                                                                                                                    // favorito.
                                databaseLocal.insertarFavorito(fav);
                            } else {
                                for (int idx = 0; idx < listaFavoritosApi.size(); idx++) { // Buscamos el favorito en la
                                                                                           // lista.
                                    if (listaFavoritosApi.get(idx).getId() == cam.getId()) { // Si encontramos el
                                                                                             // favorito...
                                        listaFavoritosApi.remove(idx); // Eliminamos el favorito de la lista.
                                        break;
                                    }
                                }
                                List<Favorito> actuales = databaseLocal.obtenerFavoritosPorUsuario(u.getId());
                                for (Favorito f : actuales) {
                                    if (f.getIdCamara() == cam.getId()) {
                                        databaseLocal.eliminarFavorito(f.getIdLocal()); // Eliminamos el favorito de la
                                                                                        // base de datos local.
                                        break;
                                    }
                                }
                            }
                            visorMapa.invalidate();
                            // Si el filtro de "Solo favoritos" está activo, refrescamos el mapa.
                            if (verSoloFavs) {
                                refrescarMarcadores();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        @Override
        public void onClose() {
        }
    }

    // Clase que representa la ventana emergente de una incidencia.
    private class VentanaDetalleIncidencia extends InfoWindow {
        private Incidencia inci;

        public VentanaDetalleIncidencia(MapView mv, Incidencia inci) {
            super(R.layout.info_window_incident, mv);
            this.inci = inci;
        }

        @Override
        public void onOpen(Object item) {
            InfoWindow.closeAllInfoWindowsOn(visorMapa);
            View v = getView();
            ((TextView) v.findViewById(R.id.inc_title)).setText(inci.getNombre());
            ((TextView) v.findViewById(R.id.inc_type)).setText(inci.getTipoIncidencia());
            ((TextView) v.findViewById(R.id.inc_cause)).setText(inci.getCausa());

            TextView tvDates = v.findViewById(R.id.inc_dates);
            String inicio = inci.getFechaInicio() != null ? inci.getFechaInicio() : "";
            String fin = inci.getFechaFin() != null ? inci.getFechaFin() : "";

            if (!inicio.isEmpty() || !fin.isEmpty()) {
                String textoFechas = "Inicio: " + limpiarFecha(inicio); // Formateamos la fecha de inicio.
                if (!fin.isEmpty() && !fin.equals("null")) {
                    textoFechas += "\nFin: " + limpiarFecha(fin); // Formateamos la fecha de fin.
                }
                tvDates.setText(textoFechas); // Asignamos el texto a la vista.
                tvDates.setVisibility(View.VISIBLE); // Mostramos la vista.
            } else {
                tvDates.setVisibility(View.GONE); // Ocultamos la vista.
            }
        }

        // Limpia la fecha para que sea legible.
        private String limpiarFecha(String f) {
            if (f == null)
                return "";
            // Si viene en formato ISO, lo simplificamos para el usuario.
            try {
                if (f.contains("T")) {
                    String[] partes = f.split("T");
                    String[] fecha = partes[0].split("-");
                    String[] hora = partes[1].split(":");
                    return fecha[2] + "/" + fecha[1] + " " + hora[0] + ":" + hora[1];
                }
            } catch (Exception e) {
            }
            return f;
        }

        @Override
        public void onClose() {
        }
    }
}
