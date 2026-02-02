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

/* Esta clase es la encargada del formulario de creación de incidencias. Recoge todos los datos de una incidencia en la carretera y los manda al servidor a través de la API.*/
public class CrearIncidenciaActivity extends AppCompatActivity {
    // Tendremos dos variables que recogerán la latitud y longitud que nos pasa el
    // mapa (cuando mantenemos pulsado creamos este "marcador" con las coordenadas).
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

        consultarCatalogosAPI(); // Con esta llamada "llenamos" el selector de tipo (si es un accidente, obras...).

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

        // Vinculamos el botón de guardar con el método que procesa el envío de datos.
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                procesarEnvioDatos();
            }
        });
    }

    // Este es el método (al que llamamos arriba) que carga los datos dinámicos de los tipos de incidencia desde el servidor.
    private void consultarCatalogosAPI() {
        InfocamServiceClient.obtenerInstancia().obtenerTiposIncidencia(sesion.getToken(),
                new ApiCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> result) {
                        // Utilizamos el adapter como "puente" entre la lista y el spinner (cogeremos los datos del servidor y los meteremos en la lista).
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this,
                                R.layout.spinner_item,
                                result);
                        adapter.setDropDownViewResource(R.layout.spinner_item);
                        selectorTipo.setAdapter(adapter);
                    }

                    // En caso de error, lo tendremos que controlar para que el usuario tenga otra opción y el desplegable no aparezca vacío.
                    @Override
                    public void onError(String error) {
                        // Si el servidor no responde o existe algún problema con la red, usamos una lista con opciones por defecto.
                        String[] respaldo = { "Accidente", "Obras", "Retención", "Clima" };
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CrearIncidenciaActivity.this,
                                R.layout.spinner_item, respaldo); // Aquí, en vez de los resultados de la llamada a la API le metemos la lista de tipos.
                        selectorTipo.setAdapter(adapter);
                    }
                });
    }

    // El método que muestra la fecha y la hora también se encargará de formatear el resultado para que coincida con el que requiere la API.
    private void mostrarSelectorFechaHora(final EditText target) {
        // Obtenemos y metemos en una variable la fecha y hora actual. Así, podremos iniciar los selectores en el momento concreto en el que se encuentra el usuario (cuando vaya a crear una incidencia).
        final Calendar calendario = Calendar.getInstance();
        int a = calendario.get(Calendar.YEAR);
        int m = calendario.get(Calendar.MONTH);
        int d = calendario.get(Calendar.DAY_OF_MONTH);

        // Comenzamos seleccionando la Fecha.
        DatePickerDialog dFecha = new DatePickerDialog(this, R.style.Theme_InfoCam_Dialog, (view, anio, mes, dia) -> {
            // Al seleccionar la fecha, la formateamos de manera temporal. En Calendar, los meses empiezan en 0 (enero sería 0).
            String f = String.format("%04d-%02d-%02d", anio, (mes + 1), dia);

            // Buscamos la hora actual para el siguiente selector.
            int h = calendario.get(Calendar.HOUR_OF_DAY);
            int min = calendario.get(Calendar.MINUTE);

            // A continuación, crearemos el de la hora.
            TimePickerDialog dHora = new TimePickerDialog(this, R.style.Theme_InfoCam_Dialog, (v, hora, minuto) -> {
                // Construímos el formato que espera la API, en ISO 8601.
                String fFinal = String.format("%sT%02d:%02d:00+01:00", f, hora, minuto);
                target.setText(fFinal); // Asignamos el resultado de ambos al campo de texto.
            }, h, min, true); // Señalando "true", le pedimos al reloj que utilice el formato de 24 horas.
            dHora.show(); // Mostramos el reloj.
        }, a, m, d);
        dFecha.show(); // Mostramos el calendario. Como vemos, está fuera del código, por lo que se lanzará antes del proceso que acabamos de comentar.
    }

   // En este método procesaremos que los datos que enviamos a la API sean correctos. Para ello, recolectaremos todo lo introducido por el usuario, verificaremos qué es y no es obligatorio y gestionaremos la llamada a la API.
    private void procesarEnvioDatos() {
        // Comenzamos extrayendo los datos de la interfaz. Cogeremos el texto, lo normalizaremos a String y borraremos los posibles espacios en blanco que haya dejado el usuario.
        String n = campoNombre.getText().toString().trim(); // Nombre.
        String c = campoCausa.getText().toString().trim(); // Causa.
        String t; // Tipo.
        String fi = campoInicio.getText().toString().trim(); // Fecha de Inicio.

        // En caso de que haya algo seleccionado, obtendremos el nombre de ese elemento en formato de texto. Si no hay nada, devolveremos una cadena vacía.
        if (selectorTipo.getSelectedItem() != null) {
            t = selectorTipo.getSelectedItem().toString();
        } else {
            t = "";
        }

        // Los campos de nombre, causa y fechad de inicio serán obligatorios.
        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(c) || TextUtils.isEmpty(fi)) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show(); // En caso de que falte rellenar alguno de estos, avisaremos al usuario con un Toast.
            return;
        }

        // A continuación, con todos los datos introducidos por el usuario, crearemos un nuevo objeto incidencia.
        Incidencia inc = new Incidencia();
        inc.setIdUsuario(sesion.obtenerUsuario().getId()); // Para saber qué usuario ha creado la incidencia, obtendremos su identificador.
        inc.setNombre(n);
        inc.setTipoIncidencia(t);
        inc.setCausa(c);
        inc.setFechaInicio(fi);
        inc.setFechaFin(campoFin.getText().toString().trim()); // Ya que el campo de fecha din no es obligatorio, solo lo introduciremos si el usuario ha añadido algo en el campo del formulario.
        inc.setLatitud(lat); // Latitud.
        inc.setLongitud(lng); // Longitud.

        btnConfirmar.setEnabled(false); // Como medida de seguridad, deshabilitamos la posibilidad de enviar un segundo formulario si el usuario le da por error doble click al botón.

        // Ahora, llamaremos a la API.
        InfocamServiceClient.obtenerInstancia().crearIncidencia(sesion.getToken(), inc, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Si la API devuelve un código 2xx, notificaremos al usuario de que se ha enviado correctamente al servidor.
                Toast.makeText(CrearIncidenciaActivity.this, "Incidencia reportada correctamente", Toast.LENGTH_SHORT).show();
                finish(); // Volvemos al mapa.
            }

            @Override
            public void onError(String error) {
                // En caso de error, volvemos a activar el botón para permitir reintentar el envío del formulario.
                btnConfirmar.setEnabled(true);
                Toast.makeText(CrearIncidenciaActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
