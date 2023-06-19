package com.yanciar.wap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<WallpaperItem> wallpaperList;
    private List<String> categoryNames;
    private CategoryClickListener listener;
    private Context context;

    public CategoryAdapter(List<String> categoryNames, CategoryClickListener listener, Context context) {
        this.categoryNames = categoryNames;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName = categoryNames.get(position);
        holder.bind(categoryName);

    }

    @Override
    public int getItemCount() {
        return categoryNames.size();
    }

    public interface CategoryClickListener {
        void onCategoryClicked(String category);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            itemView.setOnClickListener(this);
        }

        public void bind(String category) {
            categoryNameTextView.setText(category);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                String category = categoryNames.get(position);
                listener.onCategoryClicked(category);

                // Start WallpapersActivity and pass the selected category
               Intent intent = new Intent(context, CategoryWallpaper.class);
               intent.putExtra("category", category);
               context.startActivity(intent);
            }
        }
    }
}
