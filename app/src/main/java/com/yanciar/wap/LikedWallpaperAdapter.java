package com.yanciar.wap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LikedWallpaperAdapter extends RecyclerView.Adapter<LikedWallpaperAdapter.LikedWallpaperViewHolder> {
    private List<WallpaperItem> likedWallpaperList;
    private Context context;

    public LikedWallpaperAdapter(List<WallpaperItem> likedWallpaperList, Context context) {
        this.likedWallpaperList = likedWallpaperList;
        this.context = context;
    }

    @NonNull
    @Override
    public LikedWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item_layout, parent, false);
        return new LikedWallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikedWallpaperViewHolder holder, int position) {
        WallpaperItem wallpaperItem = likedWallpaperList.get(position);

        // Load the liked wallpaper image using Glide or any other image loading library
        Glide.with(context)
                .load(wallpaperItem.getImageUrl())
                .centerCrop()
                .into(holder.imageView);

        // Set the indicator visibility based on the wallpaper type
        if (wallpaperItem.isPremium()) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_premium);
        } else {
            holder.indicator.setVisibility(View.GONE);
            holder.indicator.setImageResource(R.drawable.ic_free);
        }

        // Set the favorite icon as liked
        holder.favoriteIcon.setImageResource(R.drawable.baseline_favorite_24);

        // Set click listener for the favorite icon (optional in the "Likes" fragment)
        holder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform any desired action when the favorite icon is clicked in the "Likes" fragment
            }
        });
    }

    @Override
    public int getItemCount() {
        return likedWallpaperList.size();
    }

    public static class LikedWallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView indicator;
        ImageButton favoriteIcon;

        public LikedWallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            indicator = itemView.findViewById(R.id.indicator);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }
    }
}
