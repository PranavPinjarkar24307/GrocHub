package com.example.grochub.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.adapter.WishlistAdapter;
import com.example.grochub.model.WishlistItem;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private RecyclerView rvWishlistItems;
    private LinearLayout emptyWishlistView;

    private List<WishlistItem> wishlistItems = new ArrayList<>();
    private WishlistAdapter adapter;

    public WishlistFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        rvWishlistItems = view.findViewById(R.id.rv_wishlist_items);
        emptyWishlistView = view.findViewById(R.id.ll_empty_wishlist);

        adapter = new WishlistAdapter(wishlistItems);
        rvWishlistItems.setAdapter(adapter);

        checkEmptyState();

        return view;
    }

    private void checkEmptyState() {
        if (wishlistItems.isEmpty()) {
            rvWishlistItems.setVisibility(View.GONE);
            emptyWishlistView.setVisibility(View.VISIBLE);
        } else {
            rvWishlistItems.setVisibility(View.VISIBLE);
            emptyWishlistView.setVisibility(View.GONE);
        }
    }
}
