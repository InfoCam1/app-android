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

/* FavoritosFragment: Pantalla que muestra las cámaras guardadas localmente. */
public class FavoritosFragment extends Fragment {

    private RecyclerView visorFavoritos; // El contenedor en el que se encuentra la lista.
    private AdaptadorFavoritos gestorAdaptador; // El "enlace" entre los datos y la interfaz.
    private DataRepository databaseLocal; // El acceso a la base de datos local (en SQLite).
    private SessionManager preferenciaSesion; // Gestor de inicio de sesión.
    private int idUsuarioActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estadoAnterior) { // @NonNull indica que estos parámetros NUNCA podrán ser nulos. Al ponerlos, evitamos que un error NullPointerException pueda cerrar de golpe la app.
        // "Inflamos" el XML del Fragment para convertirlo en un objeto tipo Vista.
        View vista = inflador.inflate(R.layout.fragment_favoritos, contenedor, false);

        // Obtenemos el contexto de la sesión y de la base de datos.
        preferenciaSesion = new SessionManager(getContext());
        databaseLocal = new DataRepository(getContext());

        // Recuperamos al usuario logueado. Así, podremos hacer que la sesión permanezca hasta que el usuario la cierre.
        Usuario actual = preferenciaSesion.obtenerUsuario();
        if (actual != null)
            idUsuarioActual = actual.getId(); // Si hay un usuario guardado en la sesión, lo cargamos. Si no no.

        // Configuramos el RecyclerView con la lista de los favoritos de dicho usuario.
        visorFavoritos = vista.findViewById(R.id.recyclerFavoritos);
        visorFavoritos.setLayoutManager(new LinearLayoutManager(getContext())); // LinearLayoutManager crea una lista en formato vertical.

        cargarListaDesdeSQLite(); // Cargamos los favoritos guardados en SQLite.

        return vista; // Devolvemos la vista ya completa.
    }

    // Consultamos la base de datos local y refrescamos el adaptador.
    private void cargarListaDesdeSQLite() {
        List<Favorito> favoritos = databaseLocal.obtenerFavoritosPorUsuario(idUsuarioActual); // Creamos una lista con los favoritos del usuario que tenga la sesión activa.

        if (gestorAdaptador == null) {
            // Pasamos la lista y una referencia al método que borra los favoritos.
            gestorAdaptador = new AdaptadorFavoritos(favoritos, this::accionEliminarFavorito);
            visorFavoritos.setAdapter(gestorAdaptador); // Ponemos el adaptador a la vista.
        } else { // En caso de que el adaptador ya exista, en vez de "crear" uno nuevo solo refrescamos los datos.
            gestorAdaptador.actualizarDatos(favoritos);
        }
    }

    // Este método eliminará el favorito tanto del servidor como de la base de datos local.
    private void accionEliminarFavorito(Favorito f) {
        String token = preferenciaSesion.getToken();

        // Tratamos de sincronizar con la API Rest. En este mismo momento, se borrará el favorito del servidor.
        InfocamServiceClient.obtenerInstancia().conmutarFavorito(token, f.getIdCamara(), idUsuarioActual, new ApiCallback<Void>() { // "connmutarFavorito" es el nombre que tiene el método que lidia con los favoritos en "InfocamServiceClient".
                    @Override
                    public void onSuccess(Void result) {
                        // Si la API responde correctamente, borramos el favorito de la base de datos local.
                        databaseLocal.eliminarFavorito(f.getIdLocal()); // Lo borramos a través de su id.
                        Toast.makeText(getContext(), "Favorito eliminado", Toast.LENGTH_SHORT).show(); // Avisamos al usuario.
                        cargarListaDesdeSQLite(); // Volvemos a cargar la lista desde la base de datos local.
                    }

                    @Override
                    public void onError(String error) {
                        // Si no hay internet (y no podemos sincronizar con la API), borramos igualmente para evitar que el usuario se frustre.
                        databaseLocal.eliminarFavorito(f.getIdLocal());
                        Toast.makeText(getContext(), "Borrado en la base de datos local (sin conexión)", Toast.LENGTH_SHORT).show();
                        cargarListaDesdeSQLite();
                    }
                });
    }
}
