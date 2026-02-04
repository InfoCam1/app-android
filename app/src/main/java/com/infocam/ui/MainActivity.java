package com.infocam.ui;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.infocam.R;

// Esta actividad gestiona la navegación entre los diferentes fragmentos de la aplicación.
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView menuNavegacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuNavegacion = findViewById(R.id.bottom_navigation);

        // Cargamos el fragmento principal (el mapa) en caso de que sea el primer inicio
        // de la app.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapaFragment())
                    .commit();
        }

        // Creamos un listener para capturar clics en la barra de navegación.
        menuNavegacion.setOnItemSelectedListener(item -> {
            Fragment fragmentoSeleccionado = null;
            int id = item.getItemId();

            // Dependiendo del ítem seleccionado, cargamos el fragmento correspondiente.
            if (id == R.id.nav_map) {
                fragmentoSeleccionado = new MapaFragment();
            } else if (id == R.id.nav_favoritos) {
                fragmentoSeleccionado = new FavoritosFragment();
            } else if (id == R.id.nav_perfil) {
                fragmentoSeleccionado = new PerfilFragment();
            }

            // Si el id coincide con alguno de los fragmentos, lo cargamos.
            if (fragmentoSeleccionado != null) {
                // Realizamos el cambio de fragmento.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentoSeleccionado) // Quitamos el fragmento actual del
                                                                                 // contenedor y colocamos el nuevo.
                        .commit(); // Ejecutamos el cambio.
                return true; // El click se ha gestionado correctamente.
            }
            return false;
        });
    }
}
