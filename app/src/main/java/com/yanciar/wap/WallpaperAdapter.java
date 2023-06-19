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

        import java.util.ArrayList;
        import java.util.List;


        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;



public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private List<WallpaperItem> wallpaperList;
    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences favoritesPref;
    private List<WallpaperItem> filteredWallpaperList;

    public WallpaperAdapter(List<WallpaperItem> wallpaperList, Context context) {
        this.wallpaperList = wallpaperList;
        this.context = context;
        this.filteredWallpaperList = new ArrayList<>(wallpaperList);
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

    // Filter the wallpapers based on the search query
    public void filter(String query) {
        filteredWallpaperList.clear();

        if (query.isEmpty()) {
            filteredWallpaperList.addAll(wallpaperList); // If query is empty, show all wallpapers
        } else {
            query = query.toLowerCase();
            for (WallpaperItem wallpaper : wallpaperList) {
                if (wallpaper.getTitle().toLowerCase().contains(query)) {
                    filteredWallpaperList.add(wallpaper);
                }
            }
        }

        setWallpapers(filteredWallpaperList); // Set the filtered wallpapers in the adapter
    }


    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        WallpaperItem wallpaperItem = wallpaperList.get(position);
        String imageUrl = wallpaperItem.getImageUrl();

        // Load the wallpaper image using Glide or any other image loading library
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an array from the list of image URLs
                List<String> imageUrlList = new ArrayList<>();
                for (WallpaperItem item : wallpaperList) {
                    imageUrlList.add(item.getImageUrl());
                }
                String[] imageUrlArray = imageUrlList.toArray(new String[0]);

                // Start FullScreenImageActivity
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", imageUrlArray);
                intent.putExtra("currentPosition", holder.getAdapterPosition());
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
    public void setWallpapers(List<WallpaperItem> wallpapers) {
        this.wallpaperList = wallpapers;
        notifyDataSetChanged();
    }

}
