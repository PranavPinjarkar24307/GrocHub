package com.example.grochub.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.Categories;
import com.example.grochub.R;
import com.example.grochub.adapter.HomeSliderAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private ViewPager2 homeSlider;
    private TabLayout homeSliderIndicator;
    private Handler sliderHandler = new Handler();

    // Slider Images
    private int[] sliderImages = {
            R.drawable.slider,
            R.drawable.slider,
            R.drawable.slider
    };

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ================= SLIDER =================
        homeSlider = view.findViewById(R.id.homeSlider);
        homeSliderIndicator = view.findViewById(R.id.homeSliderIndicator);

        HomeSliderAdapter sliderAdapter = new HomeSliderAdapter(sliderImages);
        homeSlider.setAdapter(sliderAdapter);

        new TabLayoutMediator(homeSliderIndicator, homeSlider,
                (tab, position) -> tab.setCustomView(R.layout.tab_dot)
        ).attach();

        homeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        // ================= SEARCH =================
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.clearFocus();

        View searchPlate = searchView.findViewById(
                androidx.appcompat.R.id.search_plate
        );
        if (searchPlate != null) {
            searchPlate.setBackground(null);
        }

        TextView searchText = searchView.findViewById(
                androidx.appcompat.R.id.search_src_text
        );
        if (searchText != null) {
            searchText.setTextSize(12);
            searchText.setHintTextColor(Color.parseColor("#BDBDBD"));
            searchText.setTextColor(Color.parseColor("#000000"));
        }

        // ================= CATEGORY CLICKS =================
        View veg = view.findViewById(R.id.category_vegetables);
        View fruits = view.findViewById(R.id.category_fruits);
        View meatEggs = view.findViewById(R.id.category_meat_eggs);
        View drinks = view.findViewById(R.id.category_drinks);
        View bakery = view.findViewById(R.id.category_bakery);

        veg.setOnClickListener(v -> openCategory("vegetables"));
        fruits.setOnClickListener(v -> openCategory("fruits"));
        meatEggs.setOnClickListener(v -> openCategory("meat_eggs"));
        drinks.setOnClickListener(v -> openCategory("drinks"));
        bakery.setOnClickListener(v -> openCategory("bakery"));

        return view;
    }

    // ================= OPEN CATEGORY =================
    private void openCategory(String categoryId) {
        Intent intent = new Intent(getActivity(), Categories.class);
        intent.putExtra("category_id", categoryId);
        startActivity(intent);
    }

    // ================= SLIDER AUTO SCROLL =================
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (homeSlider == null) return;

            int currentPos = homeSlider.getCurrentItem();
            if (currentPos == sliderImages.length - 1) {
                homeSlider.setCurrentItem(0);
            } else {
                homeSlider.setCurrentItem(currentPos + 1);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 8000);
    }
}
