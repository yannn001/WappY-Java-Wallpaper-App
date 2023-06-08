package com.yanciar.wap.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanciar.wap.R;
import com.yanciar.wap.WallpaperAdapter;
import com.yanciar.wap.WallpaperItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LikesFragment extends Fragment {

    private WallpaperAdapter wallpaperAdapter;
    private List<WallpaperItem> wallpaperItemList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SharedPreferences favoritesPref;
    private StorageReference storageReference;

    public LikesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_likes, container, false);

        // Initialize views in the onCreateView method
        recyclerView = rootView.findViewById(R.id.recyclerView2);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout2);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        wallpaperItemList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(wallpaperItemList, requireContext());
        recyclerView.setAdapter(wallpaperAdapter);

        // Swipe Refresh Layout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load initial data
        loadData();
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference firebaseCollection = db.collection("wallpapers");

        Map<String, ?> favoritesMap = favoritesPref.getAll();
        Set<String> favoriteWallpaperIds = favoritesMap.keySet();

        wallpaperItemList.clear();

        for (String wallpaperId : favoriteWallpaperIds) {
            boolean isFavorite = favoritesPref.getBoolean(wallpaperId, false);

            if (isFavorite) {
                // Retrieve the premium status from Firestore
                firebaseCollection.document(wallpaperId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                boolean isPremium = documentSnapshot.getBoolean("isPremium");

                                // Retrieve the image URL from Firebase Storage
                                final String finalWallpaperId = wallpaperId; // Create a final variable for wallpaperId
                                StorageReference imageRef = storageReference.child("wallpapers/" + finalWallpaperId);

                                // Fetch the download URL of the image file
                                imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString(); // Use a separate variable for imageUrl

                                            // Determine the file extension from the URL
                                            String fileExtension = getFileExtensionFromUrl(imageUrl);

                                            // Set the image URL based on the file extension
                                            if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                                                imageUrl += ".jpg";
                                            } else if (fileExtension.equalsIgnoreCase("png")) {
                                                imageUrl += ".png";
                                            }

                                            WallpaperItem wallpaperItem = new WallpaperItem(finalWallpaperId, imageUrl, isPremium, true);
                                            wallpaperItemList.add(wallpaperItem);
                                            wallpaperAdapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle the failure to retrieve the image URL
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle the failure to retrieve the premium status
                        });
            }
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    private String getFileExtensionFromUrl(String url) {
        int dotIndex = url.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < url.length() - 1) {
            return url.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }


    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        // Call the loadData() method to refresh the data
        loadData();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}

