package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.CartFragment;
import com.example.grochub.fragment.HomeFragment;
import com.example.grochub.fragment.ProfileFragment;
import com.example.grochub.fragment.WishlistFragment;
import com.example.grochub.fragment.OrderHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Check if user is logged in at all
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navigateToWelcome();
            return;
        }

        // 2. 🔥 MANDATORY PHONE CHECK (Firestore Gatekeeper)
        // This ensures users who bypass login screens are still caught
        checkPhoneVerification(user.getUid());

        setContentView(R.layout.activity_main);
        initUI(savedInstanceState);
    }

    private void checkPhoneVerification(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean hasPhone = documentSnapshot.exists()
                            && documentSnapshot.contains("phone")
                            && documentSnapshot.getString("phone") != null
                            && !documentSnapshot.getString("phone").isEmpty();

                    if (!hasPhone) {
                        // User is logged in but has no phone in DB -> Force verification
                        Intent intent = new Intent(this, NumberEnter.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // If network fails, you might want to allow entry OR show an error
                    Toast.makeText(this, "Profile Check Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToWelcome() {
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    private void initUI(Bundle savedInstanceState) {
        bottomNav = findViewById(R.id.bottomNav);

        // ================= BOTTOM NAV =================
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.menu_home) fragment = new HomeFragment();
            else if (id == R.id.menu_cart) fragment = new CartFragment();
            else if (id == R.id.menu_wishlist) fragment = new WishlistFragment();
            else if (id == R.id.menu_profile) fragment = new ProfileFragment();

            if (fragment != null) loadFragment(fragment);
            return true;
        });

        // ================= INITIAL SCREEN =================
        if (savedInstanceState == null) {
            boolean openCart = getIntent().getBooleanExtra("open_cart", false);
            bottomNav.setSelectedItemId(openCart ? R.id.menu_cart : R.id.menu_home);
        }

        // ================= BACK STACK LISTENER =================
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
            if (current instanceof HomeFragment) bottomNav.setSelectedItemId(R.id.menu_home);
            else if (current instanceof CartFragment) bottomNav.setSelectedItemId(R.id.menu_cart);
            else if (current instanceof WishlistFragment) bottomNav.setSelectedItemId(R.id.menu_wishlist);
            else if (current instanceof ProfileFragment) bottomNav.setSelectedItemId(R.id.menu_profile);
        });

        // ================= MODERN BACK HANDLING =================
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    moveTaskToBack(true);
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    public void openOrderHistory() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new OrderHistoryFragment())
                .addToBackStack("order_history")
                .commit();
    }
}