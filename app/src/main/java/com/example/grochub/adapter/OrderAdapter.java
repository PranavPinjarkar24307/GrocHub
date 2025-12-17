package com.example.grochub.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.model.OrderModel;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final List<OrderModel> list;

    public OrderAdapter(List<OrderModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OrderModel order = list.get(position);

        // ---------- STATUS ----------
        String status = order.status != null ? order.status : "PROCESSING";
        holder.tvStatus.setText(status);

        // Status color
        if ("DELIVERED".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F57C00")); // orange
        }

        // ---------- TOTAL ----------
        holder.tvTotal.setText("₹" + order.totalPrice);

        // ---------- ITEMS COUNT ----------
        int itemCount = order.items != null ? order.items.size() : 0;
        holder.tvItems.setText(itemCount + " items");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvStatus, tvTotal, tvItems;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvItems = itemView.findViewById(R.id.tv_order_items);
        }
    }
}
