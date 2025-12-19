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
import com.example.grochub.model.WishlistItem;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private final List<WishlistItem> items;

    public WishlistAdapter(List<WishlistItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull WishlistViewHolder holder,
            int position
    ) {

        WishlistItem item = items.get(position);

        // ================= NAME =================
        holder.tvName.setText(item.name);

        // ================= PRICE + UNIT =================
        long finalPrice = item.specialPrice > 0
                ? item.specialPrice
                : item.price;

        String priceText = "₹" + finalPrice;

        if (item.unit != null && !item.unit.isEmpty()) {
            priceText += " / " + item.unit;
        }

        holder.tvPrice.setText(priceText);

        // ================= IMAGE =================
        if (item.image != null && !item.image.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.image)
                    .placeholder(R.drawable.gray_colour)
                    .error(R.drawable.gray_colour)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.gray_colour);
        }

        // ================= CLICK → OPEN PRODUCT =================
        holder.itemView.setOnClickListener(v -> {

            if (item.productId == null || item.productId.isEmpty()) return;

            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, ProductDetailActivity.class);
            intent.putExtra("product_id", item.productId);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ================= VIEW HOLDER =================
    static class WishlistViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProduct;
        TextView tvName, tvPrice;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
