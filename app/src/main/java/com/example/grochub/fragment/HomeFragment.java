package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.AddressActivity;
import com.example.grochub.Categories;
import com.example.grochub.R;
import com.example.grochub.SearchActivity;
import com.example.grochub.adapter.HomeSliderAdapter;
import com.example.grochub.adapter.SpecialDealAdapter;
import com.example.grochub.model.HomeSliderModel;
import com.example.grochub.model.SpecialDealModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ================= SLIDER =================
    private ViewPager2 homeSlider;
    private HomeSliderAdapter sliderAdapter;
    private final List<HomeSliderModel> sliderList = new ArrayList<>();
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private LinearLayout sliderDots;
    private ImageView[] dots;

    // ================= SPECIAL DEALS =================
    private RecyclerView rvSpecialDeals;
    private SpecialDealAdapter specialDealAdapter;
    private final List<SpecialDealModel> specialDealList = new ArrayList<>();

    // ================= ADDRESS =================
    private TextView tvDeliveryAddress;
    private ImageView ivDropdownArrow;
    private String fullAddressDetails = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initSlider(view);
        initSearchBar(view);
        initCategories(view);
        initSpecialDeals(view);

        initAddress(view);

        return view;
    }

    private void initAddress(View view) {
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address);
        ivDropdownArrow = view.findViewById(R.id.iv_location_dropdown_arrow);

        loadUserAddress();

        ivDropdownArrow.setOnClickListener(v -> showAddressPopup());
        tvDeliveryAddress.setOnClickListener(v -> showAddressPopup());
    }

    private void showAddressPopup() {
        if (fullAddressDetails.isEmpty()) {
            Toast.makeText(getContext(), "Address not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delivery Location")
                .setMessage(fullAddressDetails)
                .setIcon(R.drawable.carbon_location)
                .setPositiveButton("OK", null)
                .setNeutralButton("Change", (dialog, which) -> {
                    startActivity(new Intent(getContext(), AddressActivity.class));
                })
                .show();
    }

    private void loadUserAddress() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            tvDeliveryAddress.setText("Please Login");
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("Address");

        // ⭐ KEEP DATA SYNCED LOCALLY ⭐
        // This ensures the address is available offline/after restart
        addressRef.keepSynced(true);

        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String addressLine = snapshot.child("addressLine").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String state = snapshot.child("state").getValue(String.class);
                    String pin = snapshot.child("pinCode").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    String shortText = "";
                    if (addressLine != null) shortText += addressLine;
                    if (city != null && !city.isEmpty()) shortText += ", " + city;

                    if (!shortText.isEmpty()) {
                        tvDeliveryAddress.setText(shortText);
                    } else {
                        tvDeliveryAddress.setText("Set your address");
                    }

                    StringBuilder sb = new StringBuilder();
                    if (addressLine != null) sb.append(addressLine).append("\n");
                    if (city != null) sb.append(city);
                    if (state != null) sb.append(", ").append(state);
                    if (pin != null) sb.append(" - ").append(pin);
                    if (phone != null) sb.append("\n\nPhone: ").append(phone);

                    fullAddressDetails = sb.toString();

                } else {
                    tvDeliveryAddress.setText("Set your delivery location");
                    fullAddressDetails = "No address saved.";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // ================= SLIDER =================
    private void initSlider(View view) {
        homeSlider = view.findViewById(R.id.homeSlider);
        sliderDots = view.findViewById(R.id.sliderDots);

        if (!isAdded() || getContext() == null) return;

        sliderAdapter = new HomeSliderAdapter(getContext(), sliderList);
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

        homeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
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
                    if (!isAdded() || getView() == null) return;

                    sliderList.clear();

                    if (!snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            HomeSliderModel slider = doc.toObject(HomeSliderModel.class);
                            if (slider != null) sliderList.add(slider);
                        }
                        sliderAdapter.notifyDataSetChanged();
                        setupDots(sliderList.size());
                        sliderHandler.postDelayed(sliderRunnable, 4000);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Slider load failed", e));
    }

    // ================= DOTS =================
    private void setupDots(int count) {
        if (!isAdded() || getContext() == null) return;

        sliderDots.removeAllViews();
        dots = new ImageView[count];

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageResource(R.drawable.dot);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);

            dots[i].setLayoutParams(params);
            sliderDots.addView(dots[i]);
        }

        if (count > 0) {
            dots[0].setImageResource(R.drawable.dot_selected);
            dots[0].setScaleX(1.1f);
            dots[0].setScaleY(1.1f);
        }
    }

    private void updateDots(int position) {
        if (dots == null || !isAdded()) return;

        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setImageResource(R.drawable.dot_selected);
            } else {
                dots[i].setImageResource(R.drawable.dot);
            }
        }
    }

    private final Runnable sliderRunnable = () -> {
        if (!isAdded() || homeSlider == null || sliderList.isEmpty()) return;
        int next = (homeSlider.getCurrentItem() + 1) % sliderList.size();
        homeSlider.setCurrentItem(next, true);
    };

    // ================= SEARCH =================
    private void initSearchBar(View view) {
        View searchClickLayer = view.findViewById(R.id.search_click_layer);
        searchClickLayer.setOnClickListener(v -> {
            if (!isAdded()) return;
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });
    }

    // ================= CATEGORIES =================
    private void initCategories(View view) {
        view.findViewById(R.id.category_vegetables).setOnClickListener(v -> openCategory("vegetables"));
        view.findViewById(R.id.category_fruits).setOnClickListener(v -> openCategory("fruits"));
        view.findViewById(R.id.category_meat_eggs).setOnClickListener(v -> openCategory("meat_eggs"));
        view.findViewById(R.id.category_drinks).setOnClickListener(v -> openCategory("drinks"));
        view.findViewById(R.id.category_bakery).setOnClickListener(v -> openCategory("bakery"));
    }

    private void openCategory(String categoryId) {
        if (!isAdded()) return;
        Intent intent = new Intent(getActivity(), Categories.class);
        intent.putExtra("category_id", categoryId);
        startActivity(intent);
    }

    // ================= SPECIAL DEALS =================
    private void initSpecialDeals(View view) {
        if (!isAdded() || getContext() == null) return;

        rvSpecialDeals = view.findViewById(R.id.rv_special_deals);
        rvSpecialDeals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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
                    if (!isAdded()) return;

                    specialDealList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        SpecialDealModel deal = doc.toObject(SpecialDealModel.class);
                        if (deal != null) {
                            deal.setId(doc.getId());
                            specialDealList.add(deal);
                        }
                    }
                    specialDealAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacksAndMessages(null);
    }
}