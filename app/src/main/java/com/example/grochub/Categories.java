package com.example.grochub;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.grochub.fragment.CategoryListFragment;

public class Categories extends AppCompatActivity {

    private LinearLayout catVegetables, catFruits, catMeatEggs, catDrinks, catBakery;
    private View lastSelectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories); // change to your layout name

        String categoryName = getIntent().getStringExtra("category_name");

        catVegetables = findViewById(R.id.category_vegetables);
        catFruits = findViewById(R.id.category_fruits);
        catMeatEggs = findViewById(R.id.category_meat_eggs);
        catDrinks = findViewById(R.id.category_drinks);
        catBakery = findViewById(R.id.category_bakery);

        // default: open Vegetables page
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
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // premium feel ✨
                .replace(R.id.categoryFragmentContainer, fragment)
                .commit();
    }

    private void selectCategoryView(View selected) {
        if (lastSelectedCategory != null) {
            lastSelectedCategory.setAlpha(0.7f); // dim previous
        }
        selected.setAlpha(1f);
        lastSelectedCategory = selected;
    }
}
