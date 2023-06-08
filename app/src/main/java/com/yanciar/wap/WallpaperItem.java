package com.yanciar.wap;

public class WallpaperItem {
    private String id;
    private String imageUrl;
    private boolean isPremium;
    private boolean isFavorite;

    public WallpaperItem(String id, String imageUrl, boolean isPremium, boolean isFavorite) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isPremium = isPremium;
        this.isFavorite = isFavorite;
    }

    public String getId() {
        return id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
