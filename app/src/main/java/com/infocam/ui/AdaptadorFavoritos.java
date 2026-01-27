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

/**
 * AdaptadorFavoritos: Esta clase es el "puente" entre nuestros datos (Lista de
 * Favoritos)
 * y la vista (RecyclerView).
 * 
 * Conceptos clave para DAM:
 * 1. Patrón ViewHolder: Evitamos llamadas costosas a findViewById cada vez que
 * se hace scroll.
 * 2. Inflación: Convertimos archivos XML en objetos View de Java.
 * 3. Enlace (Binding): Asignamos los datos del objeto a los elementos visuales
 * correspondientes.
 */
public class AdaptadorFavoritos extends RecyclerView.Adapter<AdaptadorFavoritos.ViewHolderFavorito> {

    private List<Favorito> listaDeFavoritos;
    private final OnClickEliminar oyenteBorrado;

    /**
     * Interfaz para delegar la lógica de borrado al Fragmento (Desacoplamiento).
     */
    public interface OnClickEliminar {
        void ejecutar(Favorito favorito);
    }

    public AdaptadorFavoritos(List<Favorito> lista, OnClickEliminar oyente) {
        this.listaDeFavoritos = lista;
        this.oyenteBorrado = oyente;
    }

    /**
     * Actualiza la colección de datos y notifica al RecyclerView para que se
     * redibuje.
     */
    public void actualizarDatos(List<Favorito> nuevaLista) {
        this.listaDeFavoritos = nuevaLista;
        notifyDataSetChanged(); // Notificamos que los datos han cambiado
    }

    @NonNull
    @Override
    public ViewHolderFavorito onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) {
        // Inflamos el layout item_favorito para crear una nueva fila
        View vistaFila = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_favorito, padre, false);
        return new ViewHolderFavorito(vistaFila);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFavorito holder, int posicion) {
        // Aquí "atamos" o vinculamos los datos del objeto Favorito a la vista
        Favorito favorito = listaDeFavoritos.get(posicion);

        holder.txtNombre.setText(favorito.getNombre());
        holder.txtDireccion.setText("Cámara de tráfico"); // Texto informativo fijo

        // Glide es la librería recomendada en Android para carga de imágenes en segundo
        // plano
        Glide.with(holder.itemView.getContext())
                .load(favorito.getUrlImagen())
                .placeholder(R.drawable.ic_marker_camera)
                .into(holder.imgCamara);

        // Configuramos el evento click del botón de papelera
        holder.btnPapelera.setOnClickListener(v -> oyenteBorrado.ejecutar(favorito));
    }

    @Override
    public int getItemCount() {
        return listaDeFavoritos.size(); // Tamaño de la lista
    }

    /**
     * Clase Interna ViewHolderFavorito:
     * Almacena las referencias a los componentes de la vista para no buscarlos
     * repetidamente.
     */
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
