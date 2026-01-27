package com.infocam.ui;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.infocam.R;

/**
 * MainActivity: Punto de entrada principal después del login.
 * 
 * Conceptos clave para DAM:
 * 1. Single Activity Architecture: Toda la aplicación ocurre en una sola
 * Activity que intercambia Fragments.
 * 2. FragmentManager: Es el motor que gestiona el intercambio de vistas
 * (Transactions).
 * 3. BottomNavigationView: Componente de Material Design para navegación
 * principal.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView menuNavegacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuNavegacion = findViewById(R.id.bottom_navigation);

        // Cargar el Fragmento principal por defecto (Mapa) si es el primer inicio
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapaFragment())
                    .commit();
        }

        // Listener para capturar clics en el menú inferior
        menuNavegacion.setOnItemSelectedListener(item -> {
            Fragment fragmentoSeleccionado = null;
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                fragmentoSeleccionado = new MapaFragment();
            } else if (id == R.id.nav_favoritos) {
                fragmentoSeleccionado = new FavoritosFragment();
            } else if (id == R.id.nav_perfil) {
                fragmentoSeleccionado = new PerfilFragment();
            }

            if (fragmentoSeleccionado != null) {
                // Realizar la transacción del fragmento
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentoSeleccionado)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
