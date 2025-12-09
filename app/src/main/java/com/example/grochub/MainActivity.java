package com.example.grochub;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.HomeFragment;
import com.example.grochub.fragment.CartFragment;
import com.example.grochub.fragment.ProfileFragment;
import com.example.grochub.fragment.WishlistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // 1️⃣ FIRST: set listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int id = item.getItemId();
            if (id == R.id.menu_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.menu_cart) {
                fragment = new CartFragment();
            } else if (id == R.id.menu_wishlist) {
                fragment = new WishlistFragment();
            } else if (id == R.id.menu_profile) {
                fragment = new ProfileFragment();
            }

            loadFragment(fragment);
            return true;
        });

        // 2️⃣ THEN: decide which tab to open (ONLY when activity created first time)
        if (savedInstanceState == null) {
            boolean openCart = getIntent().getBooleanExtra("open_cart", false);

            if (openCart) {
                bottomNav.setSelectedItemId(R.id.menu_cart);   // this will trigger listener → CartFragment
            } else {
                bottomNav.setSelectedItemId(R.id.menu_home);   // this will trigger listener → HomeFragment
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
