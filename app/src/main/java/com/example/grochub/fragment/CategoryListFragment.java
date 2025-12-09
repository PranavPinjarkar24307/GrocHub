package com.example.grochub.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.adapter.CategoryItemAdapter;
import com.example.grochub.model.CategoryItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";

    private String categoryId;
    private RecyclerView rvItems;
    private CategoryItemAdapter adapter;
    private List<CategoryItem> itemList = new ArrayList<>();

    public static CategoryListFragment newInstance(String categoryId) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public CategoryListFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_list, container, false);
        rvItems = view.findViewById(R.id.rv_category_items);

        rvItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvItems.setHasFixedSize(true);
        rvItems.setNestedScrollingEnabled(false);
        adapter = new CategoryItemAdapter(itemList);
        rvItems.setAdapter(adapter);

        loadItemsForCategory(categoryId);

        return view;
    }

    private void loadItemsForCategory(String categoryId) {
        itemList.clear();

        if ("VEGETABLES".equals(categoryId)) {
            itemList.add(new CategoryItem(R.drawable.tomatoes, "Tomato", "₹40 / kg"));
            itemList.add(new CategoryItem(R.drawable.potato, "Potato", "₹30 / kg"));
            itemList.add(new CategoryItem(R.drawable.onion, "Onion", "₹35 / kg"));
            itemList.add(new CategoryItem(R.drawable.pumpkins, "Pumpkin", "₹30 / kg"));
            itemList.add(new CategoryItem(R.drawable.ginger, "Ginger", "₹80 / kg"));
            itemList.add(new CategoryItem(R.drawable.cauliflower, "Cauliflower", "₹25 / piece"));
            itemList.add(new CategoryItem(R.drawable.cabbage, "Cabbage", "₹20 / piece"));
            itemList.add(new CategoryItem(R.drawable.carrot, "Carrot", "₹45 / kg"));
            itemList.add(new CategoryItem(R.drawable.peppers, "Peppers", "₹50 / kg"));
        } else if ("FRUITS".equals(categoryId)) {
            itemList.add(new CategoryItem(R.drawable.apple, "Apple", "₹120 / kg"));
            itemList.add(new CategoryItem(R.drawable.banana, "Banana", "₹50 / dozen"));
            itemList.add(new CategoryItem(R.drawable.oranges, "Orange", "₹80 / kg"));
            itemList.add(new CategoryItem(R.drawable.graps, "Grapes", "₹70 / kg"));
            itemList.add(new CategoryItem(R.drawable.mango, "Mango", "₹100 / kg"));
            itemList.add(new CategoryItem(R.drawable.strawberry, "Strawberry", "₹150 / box"));
            itemList.add(new CategoryItem(R.drawable.pineapple, "Pineapple", "₹60 / piece"));
        } else if ("MEAT_EGGS".equals(categoryId)) {
            itemList.add(new CategoryItem(R.drawable.slider, "Chicken", "₹220 / kg"));
            itemList.add(new CategoryItem(R.drawable.slider, "Eggs (12)", "₹70"));
        } else if ("DRINKS".equals(categoryId)) {
            itemList.add(new CategoryItem(R.drawable.slider, "Cola 1L", "₹60"));
            itemList.add(new CategoryItem(R.drawable.slider, "Orange Juice", "₹90"));
        } else if ("BAKERY".equals(categoryId)) {
            itemList.add(new CategoryItem(R.drawable.slider, "Bread", "₹35"));
            itemList.add(new CategoryItem(R.drawable.slider, "Croissant", "₹45"));
        }

        adapter.notifyDataSetChanged();
    }
}
