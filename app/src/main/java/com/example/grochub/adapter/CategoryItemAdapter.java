package com.example.grochub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.grochub.ProductDetailActivity;
import com.example.grochub.R;
import com.example.grochub.model.CategoryItem;

import java.util.List;

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ItemViewHolder> {

    private List<CategoryItem> items;

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

        Glide.with(holder.itemView.getContext())
                .load(item.getImageResId())
                .override(300, 300)
                .centerCrop()
                .into(holder.ivImage);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());

        // 🔹 FIXED CLICK HANDLER
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CategoryItem clicked = items.get(pos);

            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_NAME, clicked.getName());
            intent.putExtra(ProductDetailActivity.EXTRA_PRICE, clicked.getPrice());
            intent.putExtra(ProductDetailActivity.EXTRA_IMAGE, clicked.getImageResId());
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

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
