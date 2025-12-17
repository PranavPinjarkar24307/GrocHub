package com.example.grochub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.CategoryListFragment;

public class Categories extends AppCompatActivity {

    private LinearLayout catVegetables, catFruits, catMeatEggs, catDrinks, catBakery;
    private View lastSelectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        catVegetables = findViewById(R.id.category_vegetables);
        catFruits = findViewById(R.id.category_fruits);
        catMeatEggs = findViewById(R.id.category_meat_eggs);
        catDrinks = findViewById(R.id.category_drinks);
        catBakery = findViewById(R.id.category_bakery);

        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

        ImageView cartIcon = findViewById(R.id.iv_cart_icon);
        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Categories.this, MainActivity.class);
            intent.putExtra("open_cart", true);
            startActivity(intent);
            finish();
        });

        // ================= SEARCH BAR (BUTTON MODE – FINAL) =================
        SearchView searchView = findViewById(R.id.search_view);
        View searchOverlay = findViewById(R.id.search_click_overlay);

        // keep your style EXACTLY as you set
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackground(null);
        }

        TextView searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            searchText.setTextSize(12);
            searchText.setHintTextColor(Color.parseColor("#BDBDBD"));
            searchText.setTextColor(Color.parseColor("#000000"));
        }

        // 🔥 ONLY THIS HANDLES CLICK
        searchOverlay.setOnClickListener(v ->
                startActivity(new Intent(Categories.this, SearchActivity.class))
        );

        // ================= CATEGORY SETUP =================
        updateTabUi(catVegetables, false);
        updateTabUi(catFruits, false);
        updateTabUi(catMeatEggs, false);
        updateTabUi(catDrinks, false);
        updateTabUi(catBakery, false);

        String categoryId = getIntent().getStringExtra("category_id");
        if (categoryId == null) categoryId = "vegetables";

        openCategory(categoryId);

        switch (categoryId) {
            case "fruits":
                selectCategoryView(catFruits);
                break;
            case "meat_eggs":
                selectCategoryView(catMeatEggs);
                break;
            case "drinks":
                selectCategoryView(catDrinks);
                break;
            case "bakery":
                selectCategoryView(catBakery);
                break;
            default:
                selectCategoryView(catVegetables);
                break;
        }

        catVegetables.setOnClickListener(v -> {
            openCategory("vegetables");
            selectCategoryView(v);
        });

        catFruits.setOnClickListener(v -> {
            openCategory("fruits");
            selectCategoryView(v);
        });

        catMeatEggs.setOnClickListener(v -> {
            openCategory("meat_eggs");
            selectCategoryView(v);
        });

        catDrinks.setOnClickListener(v -> {
            openCategory("drinks");
            selectCategoryView(v);
        });

        catBakery.setOnClickListener(v -> {
            openCategory("bakery");
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
        if (lastSelectedCategory != null && lastSelectedCategory != newTab) {
            updateTabUi(lastSelectedCategory, false);
        }
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
            tab.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
            icon.setAlpha(1f);
            label.setAlpha(1f);
            label.setTextColor(getResources().getColor(R.color.category_selected));
        } else {
            tab.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            icon.setAlpha(0.6f);
            label.setAlpha(0.6f);
            label.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
