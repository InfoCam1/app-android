package com.infocam.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.infocam.R;
import com.infocam.model.Favorito;
import java.util.List;

// Esta clase es el "puente" entre nuestros datos (la lista de favoritos) y la vista (el RecyclerView). Hace que los datos puedan ser visualizados en la aplicación.
public class AdaptadorFavoritos extends RecyclerView.Adapter<AdaptadorFavoritos.ViewHolderFavorito> {
    private List<Favorito> listaDeFavoritos;
    private final OnClickEliminar listenerBorrado;

    // Esta interfaz delega la lógica del borrado, ya que no buscamos que el adaptador decida CÓMO se borra de la BBDD, sino que avise de que DEBE ser borrado por el "FavoritosFragment".
    public interface OnClickEliminar {
        void ejecutar(Favorito favorito);
    }

    // Crearemos también un constructor que usaremos a la hora de cargar los favoritos.
    public AdaptadorFavoritos(List<Favorito> lista, OnClickEliminar listener) {
        this.listaDeFavoritos = lista;
        this.listenerBorrado = listener;
    }

    // Necesitamos un método que actualice los datos y avise, a través de "notifyDataSetChanged()", a la vista para que esta recargue la pantalla.
    public void actualizarDatos(List<Favorito> nuevaLista) {
        this.listaDeFavoritos = nuevaLista;
        notifyDataSetChanged(); // Notificamos que los datos han cambiado.
    }

    @NonNull
    @Override
    public ViewHolderFavorito onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) {
        // "Inflamos" el layout "item_favorito" para crear una nueva fila. Solo se llamará las veces necesarias para llenar la pantalla.
        View vistaFila = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_favorito, padre, false); // "R.layout.item_favorito" es el diseño de UNA sola fila.
        return new ViewHolderFavorito(vistaFila);
    }

    // A continuación, tendremos que vincular los datos con su fila correspondiente. "onBindViewHolder" es un método al que se le llama constantemente (cada vez que hacemos scroll) y dice en qué fila va el nombre X y su foto.
    @Override
    public void onBindViewHolder(@NonNull ViewHolderFavorito holder, int posicion) {
        // Aquí vinculamos los datos del objeto Favorito a la vista.
        Favorito favorito = listaDeFavoritos.get(posicion);

        holder.txtNombre.setText(favorito.getNombre());
        holder.txtDireccion.setText("Cámara de tráfico"); // Ponemos una pequeña descripción. En este caso siempre indicará que se trata de una cámara de tráfico. En un futuro, si se añade la funcionalidad de poner incidencias como favoritas, deberíamos camiar también este texto.

        // Utilizamos la librería Glide para cargar las imágenes en segundo plano, sin que la aplicación sufra parones. Se encargará, no solo de cargarla en el ImageView, sino que también la redimensionará.
        Glide.with(holder.itemView.getContext())
                .load(favorito.getUrlImagen())
                .placeholder(R.drawable.placeholder_camera) // Ponemos una imagen temporal mientras se termina de cargar la real.
                .into(holder.imgCamara);

        // También tendremos que configurar qué ocurre cuando el usuario pulsa el icono de la papelera, por lo que llamamos a nuestro propio método.
        holder.btnPapelera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerBorrado.ejecutar(favorito);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaDeFavoritos.size(); // Nos dice el tamaño de la lista.
    }

    // Esta clase guarda las referencias de los botones y textos que hay colocados en el layout. Se utiliza para no tener que buscarlos cada vez que los necesitemos.
    public static class ViewHolderFavorito extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDireccion;
        ImageView imgCamara;
        ImageButton btnPapelera;

        public ViewHolderFavorito(@NonNull View vista) {
            super(vista);
            txtNombre = vista.findViewById(R.id.tvNombreFav);
            txtDireccion = vista.findViewById(R.id.tvDireccionFav);
            imgCamara = vista.findViewById(R.id.ivFotoFav);
            btnPapelera = vista.findViewById(R.id.btnEliminarFav);
        }
    }
}
