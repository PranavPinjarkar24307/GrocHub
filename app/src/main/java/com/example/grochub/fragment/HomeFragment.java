package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.Categories;
import com.example.grochub.R;
import com.example.grochub.SearchActivity;
import com.example.grochub.adapter.HomeSliderAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private ViewPager2 homeSlider;
    private TabLayout homeSliderIndicator;
    private final Handler sliderHandler = new Handler();

    private final int[] sliderImages = {
            R.drawable.slider,
            R.drawable.slider,
            R.drawable.slider
    };

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

        homeSlider.setAdapter(new HomeSliderAdapter(sliderImages));

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

        // ================= SEARCH BAR (100% BUTTON) =================
        SearchView searchView = view.findViewById(R.id.search_view);
        View searchClickLayer = view.findViewById(R.id.search_click_layer);

        // 🔒 Disable SearchView behavior ONLY (style untouched)
        searchView.setIconified(false);
        searchView.setFocusable(false);
        searchView.setFocusableInTouchMode(false);
        searchView.clearFocus();

        // ❌ Prevent SearchView from handling touch
        searchView.setOnTouchListener((v, event) -> true);

        // ✅ FULL SEARCH BAR CLICK
        searchClickLayer.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SearchActivity.class))
        );

        // ================= CATEGORY CLICKS =================
        view.findViewById(R.id.category_vegetables)
                .setOnClickListener(v -> openCategory("vegetables"));

        view.findViewById(R.id.category_fruits)
                .setOnClickListener(v -> openCategory("fruits"));

        view.findViewById(R.id.category_meat_eggs)
                .setOnClickListener(v -> openCategory("meat_eggs"));

        view.findViewById(R.id.category_drinks)
                .setOnClickListener(v -> openCategory("drinks"));

        view.findViewById(R.id.category_bakery)
                .setOnClickListener(v -> openCategory("bakery"));

        return view;
    }

    private void openCategory(String categoryId) {
        Intent intent = new Intent(getActivity(), Categories.class);
        intent.putExtra("category_id", categoryId);
        startActivity(intent);
    }

    // ================= SLIDER AUTO SCROLL =================
    private final Runnable sliderRunnable = () -> {
        if (homeSlider == null) return;

        int current = homeSlider.getCurrentItem();
        homeSlider.setCurrentItem(
                current == sliderImages.length - 1 ? 0 : current + 1
        );
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
