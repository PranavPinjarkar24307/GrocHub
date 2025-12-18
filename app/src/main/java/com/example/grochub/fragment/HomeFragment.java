package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.Categories;
import com.example.grochub.R;
import com.example.grochub.SearchActivity;
import com.example.grochub.adapter.HomeSliderAdapter;
import com.example.grochub.adapter.SpecialDealAdapter;
import com.example.grochub.model.HomeSliderModel;
import com.example.grochub.model.SpecialDealModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // 🔹 Slider
    private ViewPager2 homeSlider;
    private TabLayout homeSliderIndicator;
    private HomeSliderAdapter sliderAdapter;
    private final List<HomeSliderModel> sliderList = new ArrayList<>();
    private final Handler sliderHandler = new Handler();

    // 🔹 Special Deals
    private RecyclerView rvSpecialDeals;
    private SpecialDealAdapter specialDealAdapter;
    private final List<SpecialDealModel> specialDealList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initSlider(view);
        initSearchBar(view);
        initCategories(view);
        initSpecialDeals(view);

        return view;
    }

    // ========================= SLIDER =========================

    private void initSlider(View view) {
        homeSlider = view.findViewById(R.id.homeSlider);
        homeSliderIndicator = view.findViewById(R.id.homeSliderIndicator);

        sliderAdapter = new HomeSliderAdapter(requireContext(), sliderList);
        homeSlider.setAdapter(sliderAdapter);

        homeSlider.setClipToPadding(false);
        homeSlider.setClipChildren(false);
        homeSlider.setOffscreenPageLimit(3);
        homeSlider.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        homeSlider.setPageTransformer(transformer);

        new TabLayoutMediator(homeSliderIndicator, homeSlider,
                (tab, position) -> {}).attach();

        homeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 4000);
            }
        });

        loadHomeSliders();
    }

    private void loadHomeSliders() {
        FirebaseFirestore.getInstance()
                .collection("home_sliders")
                .whereEqualTo("active", true)
                .orderBy("order")
                .get()
                .addOnSuccessListener(snapshot -> {
                    sliderList.clear();

                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            HomeSliderModel slider = doc.toObject(HomeSliderModel.class);
                            if (slider != null) sliderList.add(slider);
                        }
                        sliderAdapter.notifyDataSetChanged();
                        sliderHandler.postDelayed(sliderRunnable, 4000);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Slider load failed", e)
                );
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (homeSlider != null && sliderList.size() > 0) {
                int next = (homeSlider.getCurrentItem() + 1) % sliderList.size();
                homeSlider.setCurrentItem(next, true);
            }
        }
    };

    // ========================= SEARCH =========================

    private void initSearchBar(View view) {
        SearchView searchView = view.findViewById(R.id.search_view);
        View searchClickLayer = view.findViewById(R.id.search_click_layer);

        searchView.setIconified(false);
        searchView.setFocusable(false);
        searchView.clearFocus();

        searchClickLayer.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SearchActivity.class))
        );
    }

    // ========================= CATEGORIES =========================

    private void initCategories(View view) {
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
    }

    private void openCategory(String categoryId) {
        Intent intent = new Intent(getActivity(), Categories.class);
        intent.putExtra("category_id", categoryId);
        startActivity(intent);
    }

    // ========================= SPECIAL DEALS =========================

    private void initSpecialDeals(View view) {
        rvSpecialDeals = view.findViewById(R.id.rv_special_deals);
        rvSpecialDeals.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false)
        );
        rvSpecialDeals.setNestedScrollingEnabled(false);

        specialDealAdapter = new SpecialDealAdapter(getContext(), specialDealList);
        rvSpecialDeals.setAdapter(specialDealAdapter);

        loadSpecialDeals();
    }

    private void loadSpecialDeals() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("isSpecial", true)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    specialDealList.clear();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            SpecialDealModel deal =
                                    doc.toObject(SpecialDealModel.class);
                            if (deal != null) specialDealList.add(deal);
                        }
                        specialDealAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Error loading deals", e)
                );
    }

    // ========================= LIFECYCLE =========================

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderList.size() > 0) {
            sliderHandler.postDelayed(sliderRunnable, 4000);
        }
    }
}
