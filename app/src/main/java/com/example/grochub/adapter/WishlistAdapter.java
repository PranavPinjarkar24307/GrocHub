package com.example.grochub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.model.WishlistItem;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<WishlistItem> items;

    public WishlistAdapter(List<WishlistItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        WishlistItem item = items.get(position);
        holder.ivProduct.setImageResource(item.getImageResId());
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProduct;
        TextView tvName, tvPrice;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
