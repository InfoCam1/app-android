package com.infocam.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infocam.R;
import com.infocam.data.SessionManager;
import com.infocam.model.Incidencia;
import com.infocam.network.ApiCallback;
import com.infocam.network.ServicioApi;

import java.util.Calendar;
import java.util.List;

/**
 * CrearIncidenciaActivity: Activity encargada del formulario de reporte de
 * incidencias.
 * 
 * Conceptos clave para DAM:
 * 1. DatePickerDialog / TimePickerDialog: Componentes estándar para entrada de
 * fechas/horas.
 * 2. Spinner: Equivale al <select> de HTML, permite elegir entre una lista de
 * opciones.
 * 3. Extras: Datos pasados de una Activity a otra mediante el Intent
 * (coordenadas en este caso).
 */
public class CrearIncidenciaActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "latitud";
    public static final String EXTRA_LNG = "longitud";

    private EditText campoNombre, campoCausa, campoInicio, campoFin;
    private Spinner selectorTipo;
    private Button btnConfirmar;
    private double lat, lng;
    private SessionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_incidencia);

        sesion = new SessionManager(this);

        // Recuperamos los datos de posición pasados desde el mapa
        lat = getIntent().getDoubleExtra(EXTRA_LAT, 0);
        lng = getIntent().getDoubleExtra(EXTRA_LNG, 0);

        campoNombre = findViewById(R.id.etNombreIncidencia);
        selectorTipo = findViewById(R.id.spTipoIncidencia);
        campoCausa = findViewById(R.id.etCausa);
        campoInicio = findViewById(R.id.etFechaInicio);
        campoFin = findViewById(R.id.etFechaFin);
        btnConfirmar = findViewById(R.id.btnGuardarIncidencia);

        consultarCatalogosAPI();

        // Al hacer click en los campos de fecha, abrimos los selectores visuales
        campoInicio.setOnClickListener(v -> mostrarSelectorFechaHora(campoInicio));
        campoFin.setOnClickListener(v -> mostrarSelectorFechaHora(campoFin));

        btnConfirmar.setOnClickListener(v -> procesarEnvioDatos());
    }

    /**
     * Carga las categorías de incidencia desde el servidor para llenar el Spinner.
     */
    private void consultarCatalogosAPI() {
        ServicioApi.obtenerInstancia().obtenerTiposIncidencia(sesion.getToken(), new ApiCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this, R.layout.spinner_item,
                        result);
                adapter.setDropDownViewResource(R.layout.spinner_item);
                selectorTipo.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                // Si el servidor no responde, usamos una lista estática de seguridad
                String[] respaldo = { "Accidente", "Obras", "Retención", "Clima" };
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this, R.layout.spinner_item,
                        respaldo);
                selectorTipo.setAdapter(adapter);
            }
        });
    }

    /**
     * Secuencia de diálogos: Seleccionar Fecha -> Seleccionar Hora -> Formatear
     * Texto.
     */
    private void mostrarSelectorFechaHora(final EditText target) {
        final Calendar calendario = Calendar.getInstance();
        int a = calendario.get(Calendar.YEAR);
        int m = calendario.get(Calendar.MONTH);
        int d = calendario.get(Calendar.DAY_OF_MONTH);

        // Paso 1: Seleccionamos el día
        DatePickerDialog dFecha = new DatePickerDialog(this, R.style.Theme_InfoCam_Dialog, (view, anio, mes, dia) -> {
            String f = String.format("%04d-%02d-%02d", anio, (mes + 1), dia);

            int h = calendario.get(Calendar.HOUR_OF_DAY);
            int min = calendario.get(Calendar.MINUTE);

            // Paso 2: Seleccionamos la hora
            TimePickerDialog dHora = new TimePickerDialog(this, R.style.Theme_InfoCam_Dialog, (v, hora, minuto) -> {
                // Formato ISO 8601 esperado por el Backend
                String fFinal = String.format("%sT%02d:%02d:00+01:00", f, hora, minuto);
                target.setText(fFinal);
            }, h, min, true);
            dHora.show();
        }, a, m, d);
        dFecha.show();
    }

    /**
     * Valida, construye el objeto Incidencia y lo envía al API.
     */
    private void procesarEnvioDatos() {
        String n = campoNombre.getText().toString().trim();
        String c = campoCausa.getText().toString().trim();
        String t = selectorTipo.getSelectedItem() != null ? selectorTipo.getSelectedItem().toString() : "";
        String fi = campoInicio.getText().toString().trim();

        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(c) || TextUtils.isEmpty(fi)) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Incidencia inc = new Incidencia();
        inc.setIdUsuario(sesion.obtenerUsuario().getId());
        inc.setNombre(n);
        inc.setTipoIncidencia(t);
        inc.setCausa(c);
        inc.setFechaInicio(fi);
        inc.setFechaFin(campoFin.getText().toString().trim());
        inc.setLatitud(lat);
        inc.setLongitud(lng);

        btnConfirmar.setEnabled(false);

        ServicioApi.obtenerInstancia().crearIncidencia(sesion.getToken(), inc, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(CrearIncidenciaActivity.this, "Reporte enviado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Volvemos al mapa
            }

            @Override
            public void onError(String error) {
                btnConfirmar.setEnabled(true);
                Toast.makeText(CrearIncidenciaActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
