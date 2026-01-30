package com.infocam.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.infocam.network.InfocamServiceClient;

import java.util.Calendar;
import java.util.List;

/* Esta clase es la encargada del formulario de creación de incidencias. Recoge todos los datos de una incidencia en la carretera y los manda al servidor a través de la API.
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
    // Tendremos dos variables que recogerán la latitud y longitud que nos pasa el mapa (cuando mantenemos pulsado creamos este "marcador" con las coordenadas).
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

        // Recuperamos los datos de posición pasados desde el mapa. ¿DÓNDE ha ocurrido?
        lat = getIntent().getDoubleExtra(EXTRA_LAT, 0);
        lng = getIntent().getDoubleExtra(EXTRA_LNG, 0);

        // Vinculamos la lógica con el diseño del layout.
        campoNombre = findViewById(R.id.etNombreIncidencia);
        selectorTipo = findViewById(R.id.spTipoIncidencia);
        campoCausa = findViewById(R.id.etCausa);
        campoInicio = findViewById(R.id.etFechaInicio);
        campoFin = findViewById(R.id.etFechaFin);
        btnConfirmar = findViewById(R.id.btnGuardarIncidencia);

        consultarCatalogosAPI(); // Con esta llamada "llenamos" el selector de tipo (si es un accidente, obres...).

        // Al hacer click en los campos de fecha, abrimos un calendario para facilitar la selección.
        campoInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFechaHora(campoInicio);
            }
        });
        campoFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFechaHora(campoFin);
            }
        });

        campoInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFechaHora(campoInicio);
            }
        });
    }

    // Este es el método (al que llamamos arriba) que carga los datos dinámicos de los tipos de incidencia desde el servidor.
    private void consultarCatalogosAPI() {
        InfocamServiceClient.obtenerInstancia().obtenerTiposIncidencia(sesion.getToken(), new ApiCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                // Utilizamos el adapter como "puente" entre la lista y el spinner (cogeremos los datos del servidor y los meteremos en la lista).
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this, R.layout.spinner_item,
                        result);
                adapter.setDropDownViewResource(R.layout.spinner_item);
                selectorTipo.setAdapter(adapter);
            }

            // En caso de error, lo tendremos que controlar para que el usuario tenga otra opción y el desplegable no aparezca vacío.
            @Override
            public void onError(String error) {
                // Si el servidor no responde o existe algún problema con la red, usamos una lista con opciones por defecto.
                String[] respaldo = { "Accidente", "Obras", "Retención", "Clima" };
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this, R.layout.spinner_item, respaldo); // Aquí, en vez de los resultados de la llamada a la API le metemos la lista de tipos.
                selectorTipo.setAdapter(adapter);
            }
        });
    }

    // El método que muestra la fecha y la hora también se encargará de formatear el resultado para que coincida con el que requiere la API.
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

        InfocamServiceClient.obtenerInstancia().crearIncidencia(sesion.getToken(), inc, new ApiCallback<Void>() {
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
