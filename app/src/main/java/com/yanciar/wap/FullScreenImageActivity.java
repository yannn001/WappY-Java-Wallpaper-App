package com.yanciar.wap;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FullScreenImageActivity extends AppCompatActivity {

    private TouchImageView imageView;
    private ImageButton buttonBack;
    private TextView buttonApply;
    private ImageButton buttonLike;

    private String imageUrl;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Retrieve the image URL from the intent
        imageUrl = getIntent().getStringExtra("imageUrl");

        // Initialize views
        imageView = findViewById(R.id.imageView);
        buttonBack = findViewById(R.id.buttonBack);
        buttonApply = findViewById(R.id.buttonApply);
        buttonLike = findViewById(R.id.buttonLike);

        // Set the image URL to the ImageView using your preferred image loading library
        Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(new RequestOptions().fitCenter())
                .into(imageView);

        // Set click listeners
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetWallpaperDialog();
            }
        });

        buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform like action
            }
        });

    }


    private void showSetWallpaperDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MinimalDialogStyle);
        builder.setTitle("Set Wallpaper");
        builder.setMessage("Set wallpaper on:");
        builder.setPositiveButton("Lockscreen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setWallpaper(WallpaperManager.FLAG_LOCK);
            }
        });
        builder.setNegativeButton("Home Screen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setWallpaper(WallpaperManager.FLAG_SYSTEM);
            }
        });
        builder.show();
    }

    private void setWallpaper(final int wallpaperFlag) {
        Thread wallpaperThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Load the image from the URL as a Bitmap
                    InputStream inputStream = new URL(imageUrl).openStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                // Get the current zoomed and panned state from the TouchImageView
                                float zoom = imageView.getCurrentZoom();
                                float translateX = imageView.getCurrentTranslateX();
                                float translateY = imageView.getCurrentTranslateY();

                                // Get the screen dimensions
                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                int screenWidth = displayMetrics.widthPixels;
                                int screenHeight = displayMetrics.heightPixels;

                                // Calculate the scaled dimensions based on the zoom level and screen size
                                int scaledWidth = Math.round(screenWidth / zoom);
                                int scaledHeight = Math.round(screenHeight / zoom);

                                // Calculate the translation offsets
                                int translatedX = Math.round(-translateX / zoom);
                                int translatedY = Math.round(-translateY / zoom);

                                // Create a new Matrix for applying transformations
                                Matrix matrix = new Matrix();
                                matrix.postScale(zoom, zoom);
                                matrix.postTranslate(translateX, translateY);

                                // Apply the transformations to the bitmap
                                Bitmap transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                                // Create a new Bitmap with the scaled dimensions
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(transformedBitmap, scaledWidth, scaledHeight, true);

                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        wallpaperManager.setBitmap(scaledBitmap, null, true, wallpaperFlag);
                                    } else {
                                        wallpaperManager.setBitmap(scaledBitmap);
                                    }
                                    Toast.makeText(getApplicationContext(), "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        wallpaperThread.start();
    }





    private Bitmap applyTransformations(Bitmap bitmap, float scaleFactor, float translateX, float translateY) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        matrix.postTranslate(translateX, translateY);

        Bitmap transformedBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(transformedBitmap);
        canvas.drawBitmap(bitmap, matrix, null);

        return transformedBitmap;
    }


}
