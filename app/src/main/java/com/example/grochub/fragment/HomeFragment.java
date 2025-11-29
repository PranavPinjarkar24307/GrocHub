package com.example.grochub.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.grochub.HomeSliderAdapter;
import com.example.grochub.R;

public class HomeFragment extends Fragment {

    private ViewPager2 homeSlider;
    private Handler sliderHandler = new Handler();

    // Use a drawable that actually exists (PNG/JPG in res/drawable)
    private int[] sliderImages = {
            R.drawable.slider,
            R.drawable.slider,
            R.drawable.slider
    };

    public HomeFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeSlider = view.findViewById(R.id.homeSlider);

        HomeSliderAdapter adapter = new HomeSliderAdapter(sliderImages);
        homeSlider.setAdapter(adapter);

        homeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        return view;
    }

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
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
