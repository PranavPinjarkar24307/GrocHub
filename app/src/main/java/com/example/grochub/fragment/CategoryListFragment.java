package com.example.grochub.fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public CategoryListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_category_list, container, false);
        rvItems = view.findViewById(R.id.rv_category_items);

        rvItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvItems.setHasFixedSize(true);
        rvItems.setNestedScrollingEnabled(false);

        adapter = new CategoryItemAdapter(itemList);
        rvItems.setAdapter(adapter);

        // 🔥 MIGRATION SWITCH
        loadItemsFromFirestore(categoryId);

        return view;
    }

    // =====================================
    // 🔥 NEW: LOAD FROM FIRESTORE
    // =====================================
    private void loadItemsFromFirestore(String categoryKey) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        itemList.clear();

        db.collection("products")
                .whereEqualTo("categoryId", categoryKey)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        Log.e("Firestore", "No products found for " + categoryKey);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshot) {
                        CategoryItem item = doc.toObject(CategoryItem.class);

                        if (item != null) {
                            item.setId(doc.getId());   // 🔥 THIS IS THE KEY LINE
                            itemList.add(item);
                        }
                    }


                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Failed to load products", e)
                );
    }
}
