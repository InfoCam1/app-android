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
import java.util.List;

/**
 * MapaFragment: Vista principal con mapa interactivo de Madrid.
 * 
 * Conceptos clave para DAM:
 * 1. OSMDroid: Alternativa Open Source a Google Maps.
 * 2. Marcadores y Overlays: Capas de información visual sobre las coordenadas.
 * 3. Permisos en Runtime: Solicitud de acceso al GPS según las políticas de
 * Android moderno.
 */
public class MapaFragment extends Fragment {

    private MapView visorMapa;
    private MyLocationNewOverlay capaPosicionUsuario;
    private SessionManager preferenciaSesion;
    private DataRepository databaseLocal;
    private List<Camara> listaFavoritosApi = new ArrayList<>();

    private View panelFiltros;
    private CheckBox checkCamaras, checkIncidenciasG, checkIncidenciasU, checkSoloFavoritos;
    private boolean verCamaras = true, verIncidenciasG = true, verIncidenciasU = true, verSoloFavs = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoAnterior) {
        // Inicialización obligatoria de Osmdroid
        Configuration.getInstance().load(getContext(),
                getContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));

        View vista = inflador.inflate(R.layout.fragment_map, contenedor, false);
        visorMapa = vista.findViewById(R.id.map);
        visorMapa.setTileSource(TileSourceFactory.MAPNIK);
        visorMapa.setMultiTouchControls(true);
        // Eliminamos botones de zoom nativos duplicados (Versión moderna no deprecated)
        visorMapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        preferenciaSesion = new SessionManager(getContext());
        databaseLocal = new DataRepository(getContext());

        // Botones de Zoom
        vista.findViewById(R.id.btnZoomIn).setOnClickListener(v -> visorMapa.getController().zoomIn());
        vista.findViewById(R.id.btnZoomOut).setOnClickListener(v -> visorMapa.getController().zoomOut());

        // Centro inicial: Madrid
        GeoPoint puntoInicio = obtenerUltimaPosicionConocida();
        if (puntoInicio == null)
            puntoInicio = new GeoPoint(40.4167, -3.7037);

        visorMapa.getController().setZoom(14.0);
        visorMapa.getController().setCenter(puntoInicio);

        verificarPermisosGps();
        configurarMenuFiltros(vista);

        // Nueva lógica de botones (UX)
        vista.findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (capaPosicionUsuario != null && capaPosicionUsuario.getMyLocation() != null) {
                visorMapa.getController().animateTo(capaPosicionUsuario.getMyLocation());
            } else {
                Toast.makeText(getContext(), "Buscando señal GPS...", Toast.LENGTH_SHORT).show();
                verificarPermisosGps(); // Reintenta activar si no estaba
            }
        });

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

    private void configurarMenuFiltros(View raiz) {
        panelFiltros = raiz.findViewById(R.id.panelFiltros);
        checkCamaras = raiz.findViewById(R.id.cbFiltroCamaras);
        checkIncidenciasG = raiz.findViewById(R.id.cbFiltroIncidenciasGov);
        checkIncidenciasU = raiz.findViewById(R.id.cbFiltroIncidenciasUser);
        checkSoloFavoritos = raiz.findViewById(R.id.cbFiltroFavoritos);

        raiz.findViewById(R.id.btnMenuFiltros).setOnClickListener(v -> {
            panelFiltros.setVisibility(panelFiltros.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        raiz.findViewById(R.id.btnCerrarFiltros).setOnClickListener(v -> {
            verCamaras = checkCamaras.isChecked();
            verIncidenciasG = checkIncidenciasG.isChecked();
            verIncidenciasU = checkIncidenciasU.isChecked();
            verSoloFavs = checkSoloFavoritos.isChecked();
            panelFiltros.setVisibility(View.GONE);
            repintarElementosEnMapa();
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
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    101);
        } else {
            activarCapaPosicion();
        }
    }

    private void activarCapaPosicion() {
        capaPosicionUsuario = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), visorMapa);
        capaPosicionUsuario.enableMyLocation();

        // Icono de usuario personalizado
        // Evitamos duplicidad desactivando los iconos por defecto de
        // dirección/precisión
        capaPosicionUsuario.setDrawAccuracyEnabled(false);
        // Usamos solo el icono de persona (punto azul/avatar) para mayor claridad
        Bitmap bUser = drawableToBitmap(ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_user));
        if (bUser != null) {
            capaPosicionUsuario.setPersonIcon(bUser);
            capaPosicionUsuario.setDirectionIcon(bUser); // Flecha blanca -> Punto azul
        }

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
        visorMapa.getOverlays().add(capaPosicionUsuario);
    }

    private void repintarElementosEnMapa() {
        visorMapa.getOverlays().clear();

        // Overlay para detectar pulsaciones largas y crear incidencias
        MapEventsOverlay capaEventos = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(visorMapa);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                lanzarNuevaIncidencia(p);
                return true;
            }
        });
        visorMapa.getOverlays().add(capaEventos);

        if (capaPosicionUsuario != null)
            visorMapa.getOverlays().add(capaPosicionUsuario);

        sincronizarYDescargar();
    }

    private void sincronizarYDescargar() {
        Usuario u = preferenciaSesion.obtenerUsuario();
        if (u == null) {
            traerCamarasServidor();
            traerIncidenciasServidor();
            return;
        }

        // Primero traemos los favoritos para saber qué icono poner a cada cámara
        InfocamServiceClient.obtenerInstancia().obtenerFavoritosUsuario(preferenciaSesion.getToken(), u.getId(),
                new ApiCallback<List<Camara>>() {
                    @Override
                    public void onSuccess(List<Camara> result) {
                        listaFavoritosApi = result;
                        databaseLocal.sincronizarConServidor(u.getId(), result);
                        traerCamarasServidor();
                        traerIncidenciasServidor();
                    }

                    @Override
                    public void onError(String error) {
                        traerCamarasServidor();
                        traerIncidenciasServidor();
                    }
                });
    }

    private void traerCamarasServidor() {
        if (!verCamaras) {
            visorMapa.invalidate();
            return;
        }

        InfocamServiceClient.obtenerInstancia().obtenerCamarasActivas(preferenciaSesion.getToken(),
                new ApiCallback<List<Camara>>() {
                    @Override
                    public void onSuccess(List<Camara> result) {
                        for (Camara c : result) {
                            boolean esFavorita = false;
                            for (Camara f : listaFavoritosApi) {
                                if (f.getId() == c.getId()) {
                                    esFavorita = true;
                                    break;
                                }
                            }

                            if (verSoloFavs && !esFavorita)
                                continue;

                            dibujarMarcadorCamara(c, esFavorita);
                        }
                        visorMapa.invalidate();
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    private void dibujarMarcadorCamara(Camara c, boolean esFavorita) {
        Marker m = new Marker(visorMapa);
        m.setPosition(new GeoPoint(c.getLatitud(), c.getLongitud()));
        m.setIcon(ContextCompat.getDrawable(getContext(),
                esFavorita ? R.drawable.ic_marker_favorite : R.drawable.ic_marker_camera));
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setInfoWindow(new VentanaDetalleCamara(visorMapa, c, esFavorita));
        visorMapa.getOverlays().add(m);
    }

    private void traerIncidenciasServidor() {
        if (verSoloFavs)
            return;
        Usuario u = preferenciaSesion.obtenerUsuario();
        Integer idU = (u != null) ? u.getId() : null;

        InfocamServiceClient.obtenerInstancia().obtenerIncidencias(preferenciaSesion.getToken(), idU,
                new ApiCallback<List<Incidencia>>() {
                    @Override
                    public void onSuccess(List<Incidencia> result) {
                        for (Incidencia i : result) {
                            // Si el filtro de cámaras está en "Solo Favoritos", ocultamos todas las
                            // incidencias
                            if (verSoloFavs)
                                continue;

                            // Filtros de tipo de incidencia
                            if (i.isOficial() && !verIncidenciasG)
                                continue;
                            if (!i.isOficial() && !verIncidenciasU)
                                continue;

                            Marker m = new Marker(visorMapa);
                            m.setPosition(new GeoPoint(i.getLatitud(), i.getLongitud()));
                            m.setTitle(i.getNombre());

                            // Lógica de iconos original
                            int resIcono = i.isOficial() ? R.drawable.ic_marker_incident
                                    : R.drawable.ic_marker_incident_user;
                            m.setIcon(ContextCompat.getDrawable(getContext(), resIcono));

                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            m.setInfoWindow(new VentanaDetalleIncidencia(visorMapa, i));
                            visorMapa.getOverlays().add(m);
                        }
                        visorMapa.invalidate();
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

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

    private Bitmap drawableToBitmap(Drawable d) {
        if (d == null)
            return null;
        Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return b;
    }

    private GeoPoint obtenerUltimaPosicionConocida() {
        if (getContext() == null || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return null;
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null)
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return (loc != null) ? new GeoPoint(loc.getLatitude(), loc.getLongitude()) : null;
    }

    // Ventanas emergentes (InfoWindows) personalizadas
    private class VentanaDetalleCamara extends InfoWindow {
        private Camara cam;
        private boolean esFav;

        public VentanaDetalleCamara(MapView mv, Camara cam, boolean esFav) {
            super(R.layout.info_window_camera, mv);
            this.cam = cam;
            this.esFav = esFav;
        }

        @Override
        public void onOpen(Object item) {
            InfoWindow.closeAllInfoWindowsOn(visorMapa);
            View v = getView();
            ((TextView) v.findViewById(R.id.bubble_title)).setText(cam.getNombre());
            ImageView img = v.findViewById(R.id.bubble_image);
            ImageButton btn = v.findViewById(R.id.bubble_favorite);

            btn.setImageResource(esFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            Glide.with(getContext()).load(cam.getImagen()).placeholder(R.drawable.ic_marker_camera).into(img);

            img.setOnClickListener(c -> {
                Intent i = new Intent(getContext(), FullScreenImageActivity.class);
                i.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, cam.getImagen());
                startActivity(i);
            });

            btn.setOnClickListener(c -> accionarFavorito(btn, (Marker) item));
        }

        private void accionarFavorito(ImageButton btn, Marker m) {
            Usuario u = preferenciaSesion.obtenerUsuario();
            if (u == null)
                return;

            InfocamServiceClient.obtenerInstancia().conmutarFavorito(preferenciaSesion.getToken(), cam.getId(), u.getId(),
                    new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            esFav = !esFav;
                            btn.setImageResource(esFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
                            m.setIcon(ContextCompat.getDrawable(getContext(),
                                    esFav ? R.drawable.ic_marker_favorite : R.drawable.ic_marker_camera));
                            visorMapa.invalidate();

                            if (esFav) {
                                Favorito fav = new Favorito(u.getId(), cam.getId(), cam.getNombre(),
                                        "Cámara de tráfico", cam.getLatitud(), cam.getLongitud(), cam.getImagen());
                                databaseLocal.insertarFavorito(fav);
                            } else {
                                List<Favorito> actuales = databaseLocal.obtenerFavoritosPorUsuario(u.getId());
                                for (Favorito f : actuales) {
                                    if (f.getIdCamara() == cam.getId()) {
                                        databaseLocal.eliminarFavorito(f.getIdLocal());
                                        break;
                                    }
                                }
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
                // Formateo simple para que sea legible
                String textoFechas = "Inicio: " + limpiarFecha(inicio);
                if (!fin.isEmpty() && !fin.equals("null")) {
                    textoFechas += "\nFin: " + limpiarFecha(fin);
                }
                tvDates.setText(textoFechas);
                tvDates.setVisibility(View.VISIBLE);
            } else {
                tvDates.setVisibility(View.GONE);
            }
        }

        private String limpiarFecha(String f) {
            if (f == null)
                return "";
            // Si viene en formato ISO, lo simplificamos (2025-01-27T10:00... -> 27/01
            // 10:00)
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
