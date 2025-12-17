package com.example.grochub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.grochub.ProductDetailActivity;
import com.example.grochub.R;
import com.example.grochub.model.CategoryItem;

import java.util.List;

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ItemViewHolder> {

    private final List<CategoryItem> items;

    public CategoryItemAdapter(List<CategoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_small, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        CategoryItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());

        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(R.drawable.gray_colour)
                .error(R.drawable.gray_colour)
                .into(holder.ivImage);

        // ✅ OPEN PRODUCT DETAIL PAGE
        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CategoryItem clickedItem = items.get(pos);
            Context ctx = v.getContext();

            Intent intent = new Intent(ctx, ProductDetailActivity.class);

            // ✅ USE SAME KEYS AS ProductDetailActivity
            intent.putExtra("product_name", clickedItem.getName());
            intent.putExtra("product_price",
                    Long.parseLong(clickedItem.getPrice().replace("₹", "")));
            intent.putExtra("product_image", clickedItem.getImage());

            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
