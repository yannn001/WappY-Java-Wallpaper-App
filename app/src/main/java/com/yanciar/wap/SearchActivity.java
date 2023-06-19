package com.yanciar.wap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WallpaperAdapter wallpaperAdapter;
    private List<WallpaperItem> wallpaperItemList;
    private List<WallpaperItem> filteredList;

    private CollectionReference wallpapersCollection;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private SharedPreferences favoritesPref;

    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout3);


        recyclerView = findViewById(R.id.recyclerView3);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        wallpaperItemList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(wallpaperItemList, this);
        recyclerView.setAdapter(wallpaperAdapter);

        filteredList = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        favoritesPref = getSharedPreferences("Favorites", MODE_PRIVATE);

        wallpapersCollection = firestore.collection("wallpapers");

        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(textWatcher);

        // Request focus and show the keyboard
        editTextSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);

        return true;
    }

    private void loadData(String query) {
        swipeRefreshLayout.setRefreshing(true);

        // Query Firestore to fetch documents with matching keyword
        wallpapersCollection.whereEqualTo("keyword", query).get().addOnCompleteListener(task -> {
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
                            String category = document.getString("category");

                            WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, title, isPremium, isFavorite, category, query);
                            wallpaperItemList.add(wallpaperItem);

                            // Check if all items have been processed
                            if (wallpaperItemList.size() == snapshot.size()) {
                                swipeRefreshLayout.setRefreshing(false);
                                wallpaperAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(e -> {
                            // Handle failure to retrieve download URL
                        });
                    }
                } else {
                    // No documents found with the specified keyword
                    swipeRefreshLayout.setRefreshing(false);
                    wallpaperAdapter.notifyDataSetChanged();
                  //  Toast.makeText(SearchActivity.this, "No wallpapers found with the keyword: " + query, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle failure to query Firestore
                swipeRefreshLayout.setRefreshing(false);
               // Toast.makeText(SearchActivity.this, "Failed to load wallpapers from Firestore", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void retrieveDownloadUrlsAndData(List<StorageReference> items) {
        AtomicInteger currentItem = new AtomicInteger();
        int totalItems = items.size();

        // Iterate through each item in the filtered list
        for (StorageReference item : items) {
            String id = item.getName();

            // Get the download URL for the image
            item.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Retrieve other data from Firestore document
                wallpapersCollection.document(id).get().addOnSuccessListener(documentSnapshot -> {
                    String title = documentSnapshot.getString("title");
                    boolean isPremium = documentSnapshot.getBoolean("isPremium");
                    boolean isFavorite = getFavoriteStatus(id);
                    String keyword = documentSnapshot.getString("keyword");
                    String category = documentSnapshot.getString("category");

                    WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, title, isPremium, isFavorite, keyword, category);
                    wallpaperItemList.add(wallpaperItem);

                    // Check if all items have been processed
                    if (currentItem.incrementAndGet() == totalItems) {
                        swipeRefreshLayout.setRefreshing(false);
                        wallpaperAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {
                    // Handle failure to retrieve Firestore document
                    currentItem.incrementAndGet();
                    if (currentItem.get() == totalItems) {
                        swipeRefreshLayout.setRefreshing(false);
                        wallpaperAdapter.notifyDataSetChanged();
                    }
                });
            }).addOnFailureListener(e -> {
                // Handle failure to retrieve download URL
                currentItem.incrementAndGet();
                if (currentItem.get() == totalItems) {
                    swipeRefreshLayout.setRefreshing(false);
                    wallpaperAdapter.notifyDataSetChanged();
                }
            });
        }
    }




    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // No implementation needed
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // No implementation needed
        }

        @Override
        public void afterTextChanged(Editable editable) {
            performSearch(editable.toString());
        }
    };

    private void performSearch(String query) {
        loadData(query);
    //   filteredList.clear();

    //   for (WallpaperItem wallpaper : wallpaperItemList) {
    //       if (wallpaper.getKeyword().toLowerCase().contains(query.toLowerCase())) {
    //           filteredList.add(wallpaper);
    //       }
    //   }

    //   wallpaperAdapter.setWallpapers(filteredList);
    //   wallpaperAdapter.notifyDataSetChanged();
    }



    private boolean getFavoriteStatus(String wallpaperId) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("favorites", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(wallpaperId, false);
    }

        public interface KeywordListener {
        void onKeywordRetrieved(String keyword);
    }
}
