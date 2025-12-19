package com.example.grochub.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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

public class CategoryItemAdapter
        extends RecyclerView.Adapter<CategoryItemAdapter.ItemViewHolder> {

    private final List<CategoryItem> items;

    public CategoryItemAdapter(List<CategoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_small, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ItemViewHolder holder,
            int position
    ) {

        CategoryItem item = items.get(position);

        // ================= NAME =================
        holder.tvName.setText(item.getName());

        // ================= PRICE LOGIC =================
        long originalPrice = item.getPrice();
        long specialPrice = item.getSpecialPrice();

        long finalPrice = specialPrice > 0 ? specialPrice : originalPrice;

        String priceText = "₹" + finalPrice;
        if (item.getUnit() != null && !item.getUnit().isEmpty()) {
            priceText += " / " + item.getUnit();
        }
        holder.tvPrice.setText(priceText);

        // ================= ORIGINAL PRICE + DISCOUNT =================
        if (specialPrice > 0 && specialPrice < originalPrice) {

            // Show original price
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText("₹" + originalPrice);
            holder.tvOriginalPrice.setPaintFlags(
                    holder.tvOriginalPrice.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );

            // Calculate discount %
            int discountPercent =
                    (int) (((originalPrice - specialPrice) * 100f) / originalPrice);

            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(discountPercent + "% OFF");

        } else {
            // Important for RecyclerView reuse
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // ================= IMAGE =================
        if (item.getImage() != null && !item.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImage())
                    .placeholder(R.drawable.gray_colour)
                    .error(R.drawable.gray_colour)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.gray_colour);
        }

        // ================= CLICK =================
        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CategoryItem clickedItem = items.get(pos);

            if (clickedItem.getId() == null || clickedItem.getId().isEmpty()) {
                return;
            }

            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, ProductDetailActivity.class);
            intent.putExtra("product_id", clickedItem.getId());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ================= VIEW HOLDER =================
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvName, tvPrice, tvOriginalPrice, tvDiscount;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
        }
    }
}
