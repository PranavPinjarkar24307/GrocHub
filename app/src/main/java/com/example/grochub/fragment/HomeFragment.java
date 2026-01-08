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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.AddressActivity;
import com.example.grochub.Categories;
import com.example.grochub.MainActivity;
import com.example.grochub.R;
import com.example.grochub.SearchActivity;
import com.example.grochub.adapter.HomeSliderAdapter;
import com.example.grochub.adapter.PopularItemAdapter;
import com.example.grochub.adapter.SpecialDealAdapter;
import com.example.grochub.model.HomeSliderModel;
import com.example.grochub.model.PopularItemModel;
import com.example.grochub.model.SpecialDealModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    // ==========================================
    // FIELD DECLARATIONS
    // ==========================================

    // --- Firebase Instances ---
    private FirebaseFirestore db;

    // --- Slider UI & Data ---
    private ViewPager2 homeSlider;
    private HomeSliderAdapter sliderAdapter;
    private final List<HomeSliderModel> sliderList = new ArrayList<>();
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private LinearLayout sliderDots;
    private ImageView[] dots;

    // --- Special Deals UI & Data ---
    private RecyclerView rvSpecialDeals;
    private SpecialDealAdapter specialDealAdapter;
    private final List<SpecialDealModel> specialDealList = new ArrayList<>();

    // --- Popular Items UI & Data ---
    private RecyclerView rvPopular;
    private PopularItemAdapter popularAdapter;
    private final List<PopularItemModel> popularList = new ArrayList<>();
    private ListenerRegistration popularListener; // For real-time updates

    // --- Address UI & Data ---
    private TextView tvDeliveryAddress;
    private ImageView ivDropdownArrow;
    private String fullAddressDetails = "";
    private ListenerRegistration addressListener; // For real-time updates

    // ==========================================
    // LIFECYCLE METHODS
    // ==========================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase instance
        db = FirebaseFirestore.getInstance();

        // Call initialization methods for each section of the UI
        initSlider(view);
        initSearchBar(view);
        initCategories(view);
        initSpecialDeals(view);
        initPopularItems(view);
        initAddress(view);
        initOrderHistory(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove Firestore listeners to prevent memory leaks and crashes
        if (popularListener != null) popularListener.remove();
        if (addressListener != null) addressListener.remove();

        // Stop the auto-sliding handler
        sliderHandler.removeCallbacksAndMessages(null);
    }

    // ==========================================
    // FEATURE: HOME SLIDER
    // Handles the top banner carousel, dots, and auto-scrolling
    // ==========================================

    private void initSlider(View view) {
        homeSlider = view.findViewById(R.id.homeSlider);
        sliderDots = view.findViewById(R.id.sliderDots);

        // Setup Adapter
        sliderAdapter = new HomeSliderAdapter(getContext(), sliderList);
        homeSlider.setAdapter(sliderAdapter);

        // Setup Animation (Scale effect on side pages)
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        homeSlider.setPageTransformer(transformer);

        // Handle Page Changes (Update dots and reset timer)
        homeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (dots == null) return;

                // Reset all dots to default state
                for (int i = 0; i < dots.length; i++) {
                    dots[i].setImageResource(R.drawable.dot);
                    animateDot(dots[i], false);
                }

                // Highlight current dot
                dots[position].setImageResource(R.drawable.dot_selected);
                animateDot(dots[position], true);

                // Reset auto-slide timer so it doesn't jump immediately after user swipe
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        // Fetch data from Firebase
        loadHomeSliders();
    }

    private void setupSliderDots(int count) {
        if (getContext() == null) return;

        sliderDots.removeAllViews();
        dots = new ImageView[count];

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageResource(R.drawable.dot);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            sliderDots.addView(dots[i], params);
        }

        // Highlight the first dot initially
        if (count > 0) {
            dots[0].setImageResource(R.drawable.dot_selected);
            animateDot(dots[0], true);
        }
    }

    private void loadHomeSliders() {
        db.collection("home_sliders")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    sliderList.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        HomeSliderModel model = doc.toObject(HomeSliderModel.class);
                        if (model != null) sliderList.add(model);
                    }

                    sliderAdapter.notifyDataSetChanged();
                    setupSliderDots(sliderList.size());

                    // Start Auto Slide
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, 3000);
                });
    }

    // Runnable to handle automatic sliding logic
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (homeSlider == null || sliderList.isEmpty()) return;

            int nextPos = homeSlider.getCurrentItem() + 1;
            if (nextPos >= sliderList.size()) {
                nextPos = 0; // Loop back to start
            }

            homeSlider.setCurrentItem(nextPos, true);
            sliderHandler.postDelayed(this, 3000); // Repeat every 3 seconds
        }
    };

    // Helper animation for dots
    private void animateDot(ImageView dot, boolean isActive) {
        float scale = isActive ? 1.4f : 1.0f;
        float alpha = isActive ? 1f : 0.5f;

        dot.animate()
                .scaleX(scale)
                .scaleY(scale)
                .alpha(alpha)
                .setDuration(250)
                .start();
    }

    // ==========================================
    // FEATURE: SEARCH & CATEGORIES
    // Handles clicking the search bar and category icons
    // ==========================================

    private void initSearchBar(View view) {
        View searchLayer = view.findViewById(R.id.search_click_layer);
        if (searchLayer != null) {
            searchLayer.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), SearchActivity.class)));
        }
    }

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

    private void openCategory(String id) {
        Intent i = new Intent(getActivity(), Categories.class);
        i.putExtra("category_id", id);
        startActivity(i);
    }

    // ==========================================
    // FEATURE: SPECIAL DEALS
    // Handles the horizontal list of special items
    // ==========================================

    private void initSpecialDeals(View view) {
        rvSpecialDeals = view.findViewById(R.id.rv_special_deals);
        rvSpecialDeals.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        specialDealAdapter = new SpecialDealAdapter(getContext(), specialDealList);
        rvSpecialDeals.setAdapter(specialDealAdapter);

        loadSpecialDeals();
    }

    private void loadSpecialDeals() {
        db.collection("products")
                .whereEqualTo("isSpecial", true)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
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

    // ==========================================
    // FEATURE: POPULAR ITEMS
    // Handles the grid list of popular items with Realtime Listener
    // ==========================================

    private void initPopularItems(View view) {
        rvPopular = view.findViewById(R.id.rv_popular_items);
        if (rvPopular == null) return;

        rvPopular.setLayoutManager(new GridLayoutManager(getContext(), 2));

        popularAdapter = new PopularItemAdapter(getContext(), popularList);
        rvPopular.setAdapter(popularAdapter);

        loadPopularItemsRealtime();
    }

    private void loadPopularItemsRealtime() {
        // Using addSnapshotListener for real-time updates
        popularListener = db.collection("products")
                .whereEqualTo("isPopular", true)
                .addSnapshotListener((snap, e) -> {

                    if (e != null || !isAdded() || snap == null) return;

                    popularList.clear();

                    for (DocumentSnapshot doc : snap) {
                        PopularItemModel model = doc.toObject(PopularItemModel.class);
                        if (model != null) {
                            model.setId(doc.getId());
                            popularList.add(model);
                        }
                    }
                    popularAdapter.notifyDataSetChanged();
                });
    }

    // ==========================================
    // FEATURE: USER ADDRESS
    // Handles displaying address and popup dialog
    // ==========================================

    private void initAddress(View view) {
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address);
        ivDropdownArrow = view.findViewById(R.id.iv_location_dropdown_arrow);

        loadUserAddress();

        // Allow changing address by clicking text or arrow
        ivDropdownArrow.setOnClickListener(v -> showAddressPopup());
        tvDeliveryAddress.setOnClickListener(v -> showAddressPopup());
    }

    private void loadUserAddress() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Listen for changes in User document to update address automatically
        addressListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    Map<String, Object> addressMap = (Map<String, Object>) snapshot.get("address");

                    if (addressMap == null) return;

                    String addressLine = (String) addressMap.get("addressLine");
                    String city = (String) addressMap.get("city");

                    // Update UI text
                    tvDeliveryAddress.setText(
                            addressLine != null && city != null
                                    ? addressLine + ", " + city
                                    : "Set your address"
                    );

                    fullAddressDetails = addressLine;
                });
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
                .setNeutralButton("Change", (d, w) ->
                        startActivity(new Intent(getContext(), AddressActivity.class)))
                .show();
    }

    // ==========================================
    // FEATURE: ORDER HISTORY
    // Handles navigation to Order History screen
    // ==========================================

    private void initOrderHistory(View view) {
        ImageView orderHistoryBtn = view.findViewById(R.id.order_history_button);

        if (orderHistoryBtn == null) {
            Log.e("HOME", "order_history_button NOT FOUND");
            return;
        }

        orderHistoryBtn.setOnClickListener(v -> {
            Log.d("HOME", "Order history clicked");

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            } else {
                Log.e("HOME", "Activity is not MainActivity");
            }
        });
    }
}