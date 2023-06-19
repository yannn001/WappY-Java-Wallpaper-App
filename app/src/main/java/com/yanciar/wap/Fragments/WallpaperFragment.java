package com.yanciar.wap.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanciar.wap.R;
import com.yanciar.wap.SearchActivity;
import com.yanciar.wap.WallpaperAdapter;
import com.yanciar.wap.WallpaperItem;

import java.io.Serializable;
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

    private Context fragmentContext;

    private ImageButton searchBtn;

    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;


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
        searchBtn = rootView.findViewById(R.id.search_btn);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        wallpaperItemList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(wallpaperItemList, requireContext());
        recyclerView.setAdapter(wallpaperAdapter);

        // Swipe Refresh Layout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("wallpapers", (Serializable) wallpaperItemList);
                startActivity(intent);
            }
        });

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

    private List<WallpaperItem> filterWallpapers(List<WallpaperItem> wallpapers, String query) {
        List<WallpaperItem> filteredList = new ArrayList<>();

        for (WallpaperItem wallpaper : wallpapers) {
            if (wallpaper.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(wallpaper);
            }
        }

        return filteredList;
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
                            boolean isFavorite = getFavoriteStatus(id, fragmentContext);


                            getPremiumStatus(id, isPremium -> {
                                String title = getTitleFromId(id);
                                getKeywordFromId(id, keyword -> {// Fetch the keyword for the wallpaper
                                    getCategoryFromId(id, category -> {
                                        WallpaperItem wallpaperItem = new WallpaperItem(id, imageUrl, title, isPremium, isFavorite, keyword, category);
                                        wallpaperItemList.add(wallpaperItem);
                                        wallpaperAdapter.notifyDataSetChanged();
                                    });
                                });
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



    private void filter(String query) {
        List<WallpaperItem> filteredList = filterWallpapers(wallpaperItemList, query);
        wallpaperAdapter.setWallpapers(filteredList);
    }

    private void getCategoryFromId(String wallpaperId, CategoryFragment.CategoryListener Listener){
        DocumentReference wallpaperDocument = wallpapersCollection.document(wallpaperId);
        wallpaperDocument.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String category = documentSnapshot.getString("category");
                        if (category != null) {
                            Listener.onCategoryRetrieved(category);
                        } else {
                            createCategory(wallpaperDocument, Listener);
                        }
                    } else {
                        createCategory(wallpaperDocument, Listener);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to retrieve the keyword
                    Listener.onCategoryRetrieved("");
                });
    }

    private void createCategory(DocumentReference wallpaperDoc, CategoryFragment.CategoryListener Listener){
        Map<String, Object> data = new HashMap<>();
        data.put("category", "Abstract");
        wallpaperDoc.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Listener.onCategoryRetrieved("Abstract");
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to create the document
                    Listener.onCategoryRetrieved("");
                });
    }

    private void getKeywordFromId(String wallpaperId, SearchActivity.KeywordListener listener) {
        DocumentReference wallpaperDocument = wallpapersCollection.document(wallpaperId);
        wallpaperDocument.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String keyword = documentSnapshot.getString("keyword");
                        if (keyword != null) {
                            listener.onKeywordRetrieved(keyword);
                        } else {
                            createKeyword(wallpaperDocument, listener);
                        }
                    } else {
                        createKeyword(wallpaperDocument, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to retrieve the keyword
                    listener.onKeywordRetrieved("");
                });
    }

    private void createKeyword(DocumentReference wallpaperDocument, SearchActivity.KeywordListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("keyword", "wallpaper");
        wallpaperDocument.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    listener.onKeywordRetrieved("wallpaper");
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to create the document
                    listener.onKeywordRetrieved("");
                });
    }





    private void refreshData() {
        isRefreshing = true;
        loadData();
    }

    private boolean getFavoriteStatus(String wallpaperId, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE);
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

    private String getTitleFromId(String id) {
        // Retrieve the title based on the id from your data source (e.g., Firestore, database)
        // Implement your logic to get the title here
        return "Title for " + id;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;
    }

}
