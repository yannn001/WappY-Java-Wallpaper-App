package com.yanciar.wap.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanciar.wap.CategoryAdapter;
import com.yanciar.wap.CategoryWallpaper;
import com.yanciar.wap.CenterZoomPageTransformer;
import com.yanciar.wap.R;
import com.yanciar.wap.WallpaperAdapter;
import com.yanciar.wap.WallpaperItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryFragment extends Fragment implements CategoryAdapter.CategoryClickListener {
    private ViewPager2 categoryViewPager;
    private CategoryAdapter categoryAdapter;

    private List<WallpaperItem> wallpaperList;
    private WallpaperAdapter wallpaperAdapter;
    private List<String> categoryNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catagory, container, false);

        // Initialize ViewPager
        categoryViewPager = view.findViewById(R.id.categoryViewPager);
        categoryViewPager.setPageTransformer(new CenterZoomPageTransformer());

        // Populate the category names (replace with your own logic to dynamically create categories)
        categoryNames = new ArrayList<>();
        categoryNames.add("Minimal");
        categoryNames.add("Abstract");
        categoryNames.add("Technology");
        categoryNames.add("Nature");
        categoryNames.add("Vehicles");
        categoryNames.add("Anime");
        categoryNames.add("Animals");
        categoryNames.add("Futuristic");
        categoryNames.add("World");
        categoryNames.add("Others");
        // Add more categories as needed...

        // Create the adapter
        categoryAdapter = new CategoryAdapter(categoryNames, this, requireContext());

        // Set the adapter to the ViewPager
        categoryViewPager.setAdapter(categoryAdapter);


        return view;
    }

    @Override
    public void onCategoryClicked(String category) {

    }

    public interface CategoryListener {
        void onCategoryRetrieved(String category);
    }
}
