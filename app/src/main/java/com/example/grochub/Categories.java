package com.example.grochub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.CartFragment;
import com.example.grochub.fragment.CategoryListFragment;

public class Categories extends AppCompatActivity {

    private LinearLayout catVegetables, catFruits, catMeatEggs, catDrinks, catBakery;
    private View lastSelectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        String categoryName = getIntent().getStringExtra("category_name");

        catVegetables = findViewById(R.id.category_vegetables);
        catFruits = findViewById(R.id.category_fruits);
        catMeatEggs = findViewById(R.id.category_meat_eggs);
        catDrinks = findViewById(R.id.category_drinks);
        catBakery = findViewById(R.id.category_bakery);

        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity (HomeFragment's activity)
            }
        });

        ImageView cartIcon = findViewById(R.id.iv_cart_icon);
        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Categories.this, MainActivity.class);
                intent.putExtra("open_cart", true);
                startActivity(intent);
                finish(); // optional: close Categories page
            }
        });
        // --- SEARCH VIEW SETUP ---
        SearchView searchView = findViewById(R.id.search_view);
        // 🔹 stop it from auto-focusing / opening keyboard
        searchView.clearFocus();

        // remove ugly gray background inside SearchView when focused
        View searchPlate = searchView.findViewById(
                androidx.appcompat.R.id.search_plate
        );
        if (searchPlate != null) {
            searchPlate.setBackground(null); // keep only your rounded white bg
        }
        // change hint & text size / color
        TextView searchText = searchView.findViewById(
                androidx.appcompat.R.id.search_src_text
        );
        if (searchText != null) {
            searchText.setTextSize(12); // smaller hint + text
            searchText.setHintTextColor(Color.parseColor("#BDBDBD")); // hint color
            searchText.setTextColor(Color.parseColor("#000000"));     // typed text color
        }

        // 1️⃣ FIRST: make ALL tabs look unselected (faded)
        updateTabUi(catVegetables, false);
        updateTabUi(catFruits, false);
        updateTabUi(catMeatEggs, false);
        updateTabUi(catDrinks, false);
        updateTabUi(catBakery, false);

        // 2️⃣ THEN: default = Vegetables selected (bright)
        openCategory("VEGETABLES");
        selectCategoryView(catVegetables);

        catVegetables.setOnClickListener(v -> {
            openCategory("VEGETABLES");
            selectCategoryView(v);
        });

        catFruits.setOnClickListener(v -> {
            openCategory("FRUITS");
            selectCategoryView(v);
        });

        catMeatEggs.setOnClickListener(v -> {
            openCategory("MEAT_EGGS");
            selectCategoryView(v);
        });

        catDrinks.setOnClickListener(v -> {
            openCategory("DRINKS");
            selectCategoryView(v);
        });

        catBakery.setOnClickListener(v -> {
            openCategory("BAKERY");
            selectCategoryView(v);
        });
    }

    private void openCategory(String categoryId) {
        Fragment fragment = CategoryListFragment.newInstance(categoryId);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.categoryFragmentContainer, fragment)
                .commit();
    }

    private void selectCategoryView(View newTab) {
        // reset previous selected
        if (lastSelectedCategory != null && lastSelectedCategory != newTab) {
            updateTabUi(lastSelectedCategory, false); // make old one faded again
        }

        // apply new selected style
        updateTabUi(newTab, true);
        lastSelectedCategory = newTab;
    }

    private void updateTabUi(View tab, boolean selected) {
        if (!(tab instanceof ViewGroup)) return;

        ViewGroup group = (ViewGroup) tab;
        if (group.getChildCount() < 2) return;

        ImageView icon = (ImageView) group.getChildAt(0);
        TextView label = (TextView) group.getChildAt(1);

        if (selected) {
            // selected: bright + slightly bigger
            tab.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
            icon.setAlpha(1f);
            label.setAlpha(1f);
            label.setTextColor(getResources().getColor(R.color.category_selected));
        } else {
            // unselected: faded + normal size
            tab.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            icon.setAlpha(0.6f);
            label.setAlpha(0.6f);
            label.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
