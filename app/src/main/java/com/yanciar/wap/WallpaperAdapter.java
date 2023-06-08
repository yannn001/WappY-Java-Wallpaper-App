package com.yanciar.wap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;


import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private List<WallpaperItem> wallpaperList;
    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences favoritesPref;



    public WallpaperAdapter(List<WallpaperItem> wallpaperList, Context context) {
        this.wallpaperList = wallpaperList;
        this.context = context;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        favoritesPref = context.getSharedPreferences("favorites", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item_layout, parent, false);
        return new WallpaperViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        WallpaperItem wallpaperItem = wallpaperList.get(position);
        String imageUrl = wallpaperItem.getImageUrl();
        // Load the wallpaper image using Glide or any other image loading library
        if (imageUrl != null && !imageUrl.isEmpty()){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(wallpaperItem.getImageUrl());
        // StorageReference imageRef = storage.getReferenceFromUrl("gs://wap-1-a9cc0.appspot.com/wallpapers/" + ".jpeg");
        Glide.with(context)
                .load(wallpaperItem.getImageUrl())  // Use the direct image URL
                .centerCrop()
                .into(holder.imageView);

    }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event here
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", wallpaperItem.getImageUrl());
                context.startActivity(intent);
            }
        });

        // Set the indicator visibility based on the wallpaper type
        if (wallpaperItem.isPremium()) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_premium);
        } else {
            holder.indicator.setVisibility(View.GONE);
            holder.indicator.setImageResource(R.drawable.ic_free);
        }

        // Set the favorite icon based on the favorite status
        if (wallpaperItem.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
        }

        // Set click listener for the favorite icon
        holder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFavoriteStatus(wallpaperItem);
            }
        });
    }


    private void toggleFavoriteStatus(WallpaperItem wallpaperItem) {
        String wallpaperId = wallpaperItem.getId();
        boolean currentFavoriteStatus = isWallpaperFavorite(wallpaperId);
        boolean newFavoriteStatus = !currentFavoriteStatus;

        wallpaperItem.setFavorite(newFavoriteStatus);
        notifyDataSetChanged();

        // Save the updated favorite status to SharedPreferences
        saveFavoriteStatus(wallpaperId, newFavoriteStatus);
    }

    private void saveFavoriteStatus(String wallpaperId, boolean isFavorite) {
        SharedPreferences.Editor editor = favoritesPref.edit();
        editor.putBoolean(wallpaperId, isFavorite);
        editor.apply();
    }

    private boolean isWallpaperFavorite(String wallpaperId) {
        return favoritesPref.getBoolean(wallpaperId, false);
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView indicator;
        ImageButton favoriteIcon;

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            indicator = itemView.findViewById(R.id.indicator);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }
    }
}
