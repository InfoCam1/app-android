package com.infocam.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.infocam.R;
import com.infocam.data.DataRepository;
import com.infocam.data.SessionManager;
import com.infocam.network.ApiCallback;
import com.infocam.network.InfocamServiceClient;
import com.infocam.model.Favorito;
import com.infocam.model.Usuario;
import java.util.List;

/**
 * FavoritosFragment: Pantalla que muestra las cámaras guardadas localmente.
 * 
 * Conceptos clave para DAM:
 * 1. RecyclerView: Componente de alta eficiencia para listas largas.
 * 2. Adaptador: El "cerebro" que vincula los objetos Java con el XML de cada
 * fila.
 * 3. Integración Local-API: Eliminamos el favorito del servidor y del SQLite
 * local.
 */
public class FavoritosFragment extends Fragment {

    private RecyclerView visorFavoritos;
    private AdaptadorFavoritos gestorAdaptador;
    private DataRepository databaseLocal;
    private SessionManager preferenciaSesion;
    private int idUsuarioActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoAnterior) {
        View vista = inflador.inflate(R.layout.fragment_favoritos, contenedor, false);

        preferenciaSesion = new SessionManager(getContext());
        databaseLocal = new DataRepository(getContext());

        Usuario actual = preferenciaSesion.obtenerUsuario();
        if (actual != null)
            idUsuarioActual = actual.getId();

        visorFavoritos = vista.findViewById(R.id.recyclerFavoritos);
        visorFavoritos.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarListaDesdeSQLite();

        return vista;
    }

    private void cargarListaDesdeSQLite() {
        List<Favorito> favoritos = databaseLocal.obtenerFavoritosPorUsuario(idUsuarioActual);

        if (gestorAdaptador == null) {
            gestorAdaptador = new AdaptadorFavoritos(favoritos, this::accionEliminarFavorito);
            visorFavoritos.setAdapter(gestorAdaptador);
        } else {
            gestorAdaptador.actualizarDatos(favoritos);
        }
    }

    private void accionEliminarFavorito(Favorito f) {
        String token = preferenciaSesion.getToken();

        // 1. Sincronizamos con el API Rest
        InfocamServiceClient.obtenerInstancia().conmutarFavorito(token, f.getIdCamara(), idUsuarioActual,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // 2. Si el API responde OK, borramos de la base de datos local
                        databaseLocal.eliminarFavorito(f.getIdLocal());
                        Toast.makeText(getContext(), "Favorito eliminado", Toast.LENGTH_SHORT).show();
                        cargarListaDesdeSQLite();
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback: Si no hay internet, borramos igualmente para evitar frustración al
                        // usuario
                        databaseLocal.eliminarFavorito(f.getIdLocal());
                        Toast.makeText(getContext(), "Borrado local (sin conexión)", Toast.LENGTH_SHORT).show();
                        cargarListaDesdeSQLite();
                    }
                });
    }
}
