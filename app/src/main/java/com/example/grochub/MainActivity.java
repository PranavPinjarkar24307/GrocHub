package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.CartFragment;
import com.example.grochub.fragment.HomeFragment;
import com.example.grochub.fragment.ProfileFragment;
import com.example.grochub.fragment.WishlistFragment;
import com.example.grochub.fragment.OrderHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 AUTH CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Loginpage.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

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

            if (fragment != null) {
                loadFragment(fragment);
            }

            return true;
        });

        if (savedInstanceState == null) {
            boolean openCart = getIntent().getBooleanExtra("open_cart", false);

            if (openCart) {
                bottomNav.setSelectedItemId(R.id.menu_cart);
            } else {
                bottomNav.setSelectedItemId(R.id.menu_home);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    // 🔥 ORDER HISTORY (SECONDARY SCREEN)
    public void openOrderHistory() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new OrderHistoryFragment())
                .addToBackStack("order_history")
                .commit();
    }
}
