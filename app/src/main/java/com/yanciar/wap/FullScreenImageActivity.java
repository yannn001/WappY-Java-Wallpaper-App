package com.yanciar.wap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FullScreenImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton buttonBack;
    private TextView buttonApply;
    private ImageButton buttonLike;

    private List<String> imageUrls;
    private int currentPosition;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);


        // Retrieve the image URLs and current position from the intent
        String[] urlsArray = getIntent().getStringArrayExtra("imageUrl");
        if (urlsArray != null) {
            imageUrls = Arrays.asList(urlsArray);
        } else {
            imageUrls = new ArrayList<>();
        }
        currentPosition = getIntent().getIntExtra("currentPosition", 0);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        buttonBack = findViewById(R.id.buttonBack);
        buttonApply = findViewById(R.id.buttonApply);
        buttonLike = findViewById(R.id.buttonLike);

        // Set up the ViewPager adapter
        ImagePagerAdapter adapter = new ImagePagerAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MaterialAlertDialogStyle);

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        builder.setView(dialogView);

        // Set the title and message
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        Button LockScreen = dialogView.findViewById(R.id.btn_lock);
        Button HomeScreen = dialogView.findViewById(R.id.btn_home);
        titleTextView.setText("Set Wallpaper");
        messageTextView.setText("Where would you like to apply this wallpaper?");

        // Create the AlertDialog
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Set the custom background shape

        // Set Listener for buttons
        LockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper(WallpaperManager.FLAG_LOCK);
                dialog.dismiss(); // Dismiss the dialog
                showProgressDialog();
            }
        });

        HomeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper(WallpaperManager.FLAG_SYSTEM);
                dialog.dismiss(); // Dismiss the dialog
                showProgressDialog();
            }
        });

        // Show the AlertDialog
        dialog.show();
    }




    private void setWallpaper(final int wallpaperFlag) {
        // Retrieve the current image URL from the adapter
        String imageUrl = imageUrls.get(viewPager.getCurrentItem());

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
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        wallpaperManager.setBitmap(bitmap, null, true, wallpaperFlag);
                                    } else {
                                        wallpaperManager.setBitmap(bitmap);
                                    }
                                    Toast.makeText(getApplicationContext(), "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                } finally {
                                    dismissProgressDialog(); // Dismiss the progress dialog after wallpaper setting
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                                dismissProgressDialog(); // Dismiss the progress dialog if image loading fails
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    dismissProgressDialog(); // Dismiss the progress dialog if there is an exception
                }
            }
        });

        wallpaperThread.start();
    }
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting wallpaper...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void dismissProgressDialog() {
        // Dismiss the progress dialog if it's showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
