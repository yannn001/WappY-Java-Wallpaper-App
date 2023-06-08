package com.yanciar.wap;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanciar.wap.Fragments.CategoryFragment;
import com.yanciar.wap.Fragments.LikesFragment;
import com.yanciar.wap.Fragments.WallpaperFragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Make status bar icons dark (black) on a light (white) background
        // Set status bar color to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
            // window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }


        // Set the activity title
        setTitle("WaPPy");

        // Apply a bold style to the title
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set the home fragment as the default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new WallpaperFragment())
                .commit();


    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                selectedFragment = new WallpaperFragment();
                break;
            case R.id.navigation_categories:
                selectedFragment = new CategoryFragment();
                break;
            case R.id.navigation_likes:
                selectedFragment = new LikesFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, selectedFragment)
                .commit();

        return true;
    }

}
