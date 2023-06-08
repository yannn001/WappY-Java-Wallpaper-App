package com.yanciar.wap.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanciar.wap.R;
import com.yanciar.wap.WallpaperAdapter;
import com.yanciar.wap.WallpaperItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallpaperFragment extends Fragment {

    private WallpaperAdapter wallpaperAdapter;
    private List<WallpaperItem> wallpaperItemList;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CollectionReference wallpapersCollection;
    private RecyclerView recyclerView;
    private boolean isRefreshing;

    public WallpaperFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wallpaper, container, false);

        // Initialize views in the onCreateView method
        recyclerView = rootView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        wallpaperItemList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(wallpaperItemList, requireContext());
        recyclerView.setAdapter(wallpaperAdapter);

        // Swipe Refresh Layout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Firebase Collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        wallpapersCollection = db.collection("wallpapers");

        // Load initial data
        loadData();

        return rootView;
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        // Get a reference to the wallpapers folder in Firebase Storage
        StorageReference wallpapersRef = FirebaseStorage.getInstance().getReference("wallpapers");

        wallpapersRef.listAll()
                .addOnSuccessListener(listResult -> {
                    wallpaperItemList.clear();

                    for (StorageReference item : listResult.getItems()) {
                        String id = item.getName();
                        // Use the getDownloadUrl() method to retrieve the download URL for the image
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // You can set the premium flag based on your requirements
                            boolean isFavorite = getFavoriteStatus(id);

                            getPremiumStatus(id, isPremium -> {
                                WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, isPremium, isFavorite);
                                wallpaperItemList.add(wallpaperItem);
                                wallpaperAdapter.notifyDataSetChanged();
                            });
                        }).addOnFailureListener(e -> {
                            // Handle the failure to get the download URL
                        });
                    }

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFragmentAttached()) {
                        Toast.makeText(requireContext(), "Failed to load wallpapers", Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    private void refreshData() {
        if (isRefreshing) {
            return; // Already refreshing, ignore the request
        }

        swipeRefreshLayout.setRefreshing(true);
        isRefreshing = true;

        // Get a reference to the wallpapers folder in Firebase Storage
        StorageReference wallpapersRef = FirebaseStorage.getInstance().getReference("wallpapers");

        wallpapersRef.listAll()
                .addOnSuccessListener(listResult -> {
                    wallpaperItemList.clear();

                    for (StorageReference item : listResult.getItems()) {
                        String id = item.getName();
                        // Use the getDownloadUrl() method to retrieve the download URL for the image
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // You can set the premium flag based on your requirements
                            boolean isFavorite = getFavoriteStatus(id);

                            getPremiumStatus(id, isPremium -> {
                                WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, isPremium, isFavorite);
                                wallpaperItemList.add(wallpaperItem);
                                wallpaperAdapter.notifyDataSetChanged();
                            });

                        }).addOnFailureListener(e -> {
                            // Handle the failure to get the download URL
                        });
                    }

                    if (isFragmentAttached()) {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    isRefreshing = false;
                })
                .addOnFailureListener(e -> {
                    if (isFragmentAttached()) {
                        Toast.makeText(requireContext(), "Failed to refresh wallpapers", Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    isRefreshing = false;
                });
    }

    private boolean getFavoriteStatus(String wallpaperId) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(wallpaperId, false);
    }

    private void updatePremiumStatus(String wallpaperId, boolean isPremium) {
        CollectionReference wallpapersCollection = FirebaseFirestore.getInstance().collection("wallpapers");
        DocumentReference wallpaperDocument = wallpapersCollection.document(wallpaperId);

        Map<String, Object> data = new HashMap<>();
        data.put("isPremium", isPremium);
        wallpaperDocument.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Premium status updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to update premium status
                });
    }

    private void getPremiumStatus(String wallpaperId, PremiumStatusCallback callback) {
        DocumentReference wallpaperDocument = wallpapersCollection.document(wallpaperId);
        wallpaperDocument.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isPremium = documentSnapshot.getBoolean("isPremium");
                        callback.onPremiumStatusReceived(isPremium);
                    } else {
                        // Document doesn't exist, create a new document with isPremium field set to false
                        Map<String, Object> data = new HashMap<>();
                        data.put("isPremium", false);
                        wallpaperDocument.set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    callback.onPremiumStatusReceived(false);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the failure to create the document
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to retrieve the premium status
                });
    }

    private boolean isFragmentAttached() {
        return isAdded() && getActivity() != null;
    }

    interface PremiumStatusCallback {
        void onPremiumStatusReceived(boolean isPremium);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeRefreshLayout = null;
        recyclerView = null;

        }
    }
