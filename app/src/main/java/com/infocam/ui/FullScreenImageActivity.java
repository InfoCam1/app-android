package com.infocam.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.infocam.R;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        photoView = findViewById(R.id.ivFullScreen);
        ImageButton btnClose = findViewById(R.id.btnClose);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Aseguramos que la URL sea válida y añadimos el parámetro de tiempo para
            // evitar caché
            String cacheBusterUrl = imageUrl + (imageUrl.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();

            Glide.with(this)
                    .load(cacheBusterUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(photoView);
        }

        // PhotoView ya gestiona internamente los gestos de zoom y panning.
        // No es necesario ScaleGestureDetector ni manual onTouchEvent.

        btnClose.setOnClickListener(v -> finish());
    }
}
