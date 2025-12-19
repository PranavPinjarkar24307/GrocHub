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
import com.example.grochub.model.SpecialDealModel;

import java.util.List;

public class SpecialDealAdapter
        extends RecyclerView.Adapter<SpecialDealAdapter.ViewHolder> {

    private final List<SpecialDealModel> list;
    private final Context context;

    public SpecialDealAdapter(Context context, List<SpecialDealModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_special_deal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        SpecialDealModel item = list.get(position);

        // ✅ TEXT
        holder.name.setText(item.getName());
        holder.unit.setText(item.getUnit());
        holder.specialPrice.setText("₹" + item.getSpecialPrice());
        holder.originalPrice.setText("₹" + item.getPrice());

        // ✅ STRIKE THROUGH
        holder.originalPrice.setPaintFlags(
                holder.originalPrice.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG
        );

        // ✅ OFFER %
        if (item.getOfferPercent() > 0) {
            holder.percentage.setText(item.getOfferPercent() + "% OFF");
            holder.percentage.setVisibility(View.VISIBLE);
        } else {
            holder.percentage.setVisibility(View.GONE);
        }

        // ✅ IMAGE
        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.gray_colour)
                .error(R.drawable.gray_colour)
                .into(holder.image);

        // 🔥 CLICK → PRODUCT DETAIL (PERMANENT ID)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", item.getId()); // ✅ CORRECT
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name, unit, specialPrice, originalPrice, percentage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_offer_image);
            name = itemView.findViewById(R.id.tv_offer_name);
            unit = itemView.findViewById(R.id.tv_offer_unit);
            specialPrice = itemView.findViewById(R.id.tv_special_price);
            originalPrice = itemView.findViewById(R.id.tv_original_price);
            percentage = itemView.findViewById(R.id.tv_offer_percentage);
        }
    }
}
