package com.example.grochub.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.adapter.WishlistAdapter;
import com.example.grochub.model.WishlistItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private static final String TAG = "WISHLIST_DEBUG";

    private RecyclerView rvWishlistItems;
    private LinearLayout emptyWishlistView;

    private final List<WishlistItem> wishlistItems = new ArrayList<>();
    private WishlistAdapter adapter;

    private ListenerRegistration wishlistListener;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        rvWishlistItems = view.findViewById(R.id.rv_wishlist_items);
        emptyWishlistView = view.findViewById(R.id.ll_empty_wishlist);

        rvWishlistItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvWishlistItems.setHasFixedSize(true);

        adapter = new WishlistAdapter(wishlistItems);
        rvWishlistItems.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startWishlistListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (wishlistListener != null) {
            wishlistListener.remove();
            wishlistListener = null;
        }
    }

    // ==================================================
    // 🔥 REALTIME LISTENER (UI FORCED)
    // ==================================================
    private void startWishlistListener() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        wishlistListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("wishlist")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) {
                        Log.e(TAG, "Listener error", error);
                        showEmptyState();
                        return;
                    }

                    wishlistItems.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        WishlistItem item = doc.toObject(WishlistItem.class);
                        if (item != null) {
                            wishlistItems.add(item);
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Adapter size = " + wishlistItems.size());
                        adapter.notifyDataSetChanged();

                        if (wishlistItems.isEmpty()) {
                            showEmptyState();
                        } else {
                            showListState();
                        }
                    });
                });
    }

    // ==================================================
    // UI STATES (FORCED)
    // ==================================================
    private void showEmptyState() {
        rvWishlistItems.setVisibility(View.GONE);
        emptyWishlistView.setVisibility(View.VISIBLE);
    }

    private void showListState() {
        emptyWishlistView.setVisibility(View.GONE);
        rvWishlistItems.setVisibility(View.VISIBLE);
    }
}
