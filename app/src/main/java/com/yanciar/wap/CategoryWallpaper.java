package com.yanciar.wap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryWallpaper extends AppCompatActivity {

    private static final String TAG = "CategoryWallpaper";
    private RecyclerView recyclerView;

    private CollectionReference wallpapersCollection = FirebaseFirestore.getInstance().collection("wallpapers");

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private SharedPreferences favoritesPref;
    private FirebaseFirestore firestore;
    private List<WallpaperItem> wallpaperItemList;
    private WallpaperAdapter wallpaperAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_wall);

        recyclerView = findViewById(R.id.wallpaperRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        favoritesPref = getSharedPreferences("favorites", MODE_PRIVATE);




        List<WallpaperItem> wallpapers = (List<WallpaperItem>) getIntent().getSerializableExtra("wallpapers");
        if (wallpapers != null) {
        } else {
            String selectedCategory = getIntent().getStringExtra("category");
            if (selectedCategory != null) {
               loadData(selectedCategory);
                // Set the category name on the app bar
                TextView appNameTextView = findViewById(R.id.appNameTextView3);
                appNameTextView.setText(selectedCategory);
            }
        }

        firestore = FirebaseFirestore.getInstance();
        // Initialize the wallpapersCollection field
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        wallpapersCollection = db.collection("wallpapers");
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        wallpaperItemList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(wallpaperItemList, this);


        recyclerView.setAdapter(wallpaperAdapter);

    }


    private void loadData(String category) {
            wallpapersCollection
                    .whereEqualTo("category", category)
                    .get()
                    .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    wallpaperItemList.clear();

                    // Iterate through each document
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        String id = document.getId();

                        // Get the image URL from Firebase Storage
                        StorageReference imageRef = firebaseStorage.getReference("wallpapers/" + id);
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Extract other data from the Firestore document
                            String title = document.getString("title");
                            boolean isPremium = document.getBoolean("isPremium");
                            boolean isFavorite = getFavoriteStatus(id);
                            String keyword = document.getString("keyword");

                            WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, title, isPremium, isFavorite, keyword, category);
                            wallpaperItemList.add(wallpaperItem);

                            // Check if all items have been processed
                            if (wallpaperItemList.size() == snapshot.size()) {
                                wallpaperAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(e -> {
                            // Handle failure to retrieve download URL
                        });
                    }
                } else {
                    // No documents found with the specified category
                    wallpaperAdapter.notifyDataSetChanged();
                    // Handle no wallpapers found with the category
                }
            } else {
                // Handle failure to query Firestore
                // Handle failure to load wallpapers from Firestore
            }
        });
    }

    private void displayWallpapers(List<WallpaperItem> wallpapers) {
        wallpaperItemList.clear();
        wallpaperItemList.addAll(wallpapers);
        wallpaperAdapter.notifyDataSetChanged();

    }
    private boolean getFavoriteStatus(String wallpaperId) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("favorites", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(wallpaperId, false);
    }

    private String getTitleFromId(String id) {
        // Retrieve the title based on the id from your data source (e.g., Firestore, database)
        // Implement your logic to get the title here
        return "Title for " + id;
    }
    }
