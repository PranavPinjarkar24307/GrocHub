package com.example.grochub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.grochub.R;
import com.example.grochub.model.HomeSliderModel;

import java.util.List;

public class HomeSliderAdapter extends RecyclerView.Adapter<HomeSliderAdapter.SliderViewHolder> {

    private final List<HomeSliderModel> sliderList;
    private final Context context;

    public HomeSliderAdapter(Context context, List<HomeSliderModel> sliderList) {
        this.context = context;
        this.sliderList = sliderList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        HomeSliderModel model = sliderList.get(position);

        Glide.with(context)
                .load(model.getImageUrl())
                .placeholder(R.drawable.slider) // fallback
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return sliderList.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_slider_image);
        }
    }
}
