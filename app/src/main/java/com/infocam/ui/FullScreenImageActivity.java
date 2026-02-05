package com.infocam.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.infocam.R;

// Esta actividad se encarga de mostrar la imagen en pantalla completa con Glide. Se puede hacer zoom y arrastrar la imagen, ambas cosas gracias a PhotoView.

public class FullScreenImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "extra_image_url"; // La clave que utilizaremos para recibir la URL de
                                                                    // la imagen a través del Intent.

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Iniciamos las vistas.
        photoView = findViewById(R.id.ivFullScreen);
        ImageButton btnClose = findViewById(R.id.btnClose);

        // Obtenemos la URL de la imagen pasada desde la actividad anterior.
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Aseguramos que la URL sea válida y añadimos el parámetro de tiempo para
            // evitar caché. Al añadir un parámetro de tiempo, forzamos al servidor a
            // entregar la versión más actualizada de la imagen.
            String cacheBusterUrl = imageUrl + (imageUrl.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();

            // Cargamos la imagen con Glide.
            Glide.with(this)
                    .load(cacheBusterUrl)
                    // Desactivamos el caché de disco y memoria para asegurar que siempre se cargue
                    // la imagen más reciente.
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(photoView);
        }

        // PhotoView ya gestiona internamente los gestos de zoom y panning.

        btnClose.setOnClickListener(v -> finish());
    }
}
