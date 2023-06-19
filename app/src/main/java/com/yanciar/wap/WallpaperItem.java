package com.yanciar.wap;



import java.io.Serializable;

public class WallpaperItem implements Serializable {
    private String id;
    private String imageUrl;
    private String title;
    private boolean isPremium;
    private boolean isFavorite;
    private String keyword;

    private String category;

    public WallpaperItem() {
        // Default constructor required for Firestore deserialization
    }

    public WallpaperItem(String id, String imageUrl, String title, boolean isPremium, boolean isFavorite, String keyword, String category) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.isPremium = isPremium;
        this.isFavorite = isFavorite;
        this.keyword = keyword;
        this.category = category;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
