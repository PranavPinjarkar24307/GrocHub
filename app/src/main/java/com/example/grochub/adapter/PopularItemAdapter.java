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
import com.example.grochub.model.PopularItemModel;

import java.util.List;

public class PopularItemAdapter
        extends RecyclerView.Adapter<PopularItemAdapter.ViewHolder> {

    private final List<PopularItemModel> list;
    private final Context context;

    public PopularItemAdapter(Context context, List<PopularItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_popular, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // SAFETY RULE (never remove this)
        if (position >= list.size()) return;

        PopularItemModel item = list.get(position);

        holder.tvName.setText(item.name);
        holder.tvUnit.setText(item.unit);
        holder.tvPrice.setText("₹" + item.price);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", item.getId());
            context.startActivity(intent);
        });


        Glide.with(context)
                .load(item.image)
                .placeholder(R.drawable.gray_colour) // add placeholder drawable
                .error(R.drawable.gray_colour)
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ---------------- VIEW HOLDER ----------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvName, tvUnit, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
