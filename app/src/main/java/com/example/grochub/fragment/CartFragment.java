package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.CheckoutActivity;
import com.example.grochub.R;
import com.example.grochub.adapter.CartAdapter;
import com.example.grochub.model.CartFirebaseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private View btnCheckout;

    private CartAdapter adapter;
    private final ArrayList<CartFirebaseModel> cartList = new ArrayList<>(); // Changed List to ArrayList for Parcelable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CartAdapter(cartList);
        rvCartItems.setAdapter(adapter);

        loadCartFromFirebase();

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                checkoutOrder();
            }
        });

        return view;
    }

    private void loadCartFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    cartList.clear();
                    int total = 0;

                    for (var doc : value.getDocuments()) {
                        CartFirebaseModel item = doc.toObject(CartFirebaseModel.class);
                        if (item == null) continue;

                        cartList.add(item);

                        String priceStr = item.price.replace("₹", "").split("/")[0].trim();
                        try {
                            int price = Integer.parseInt(priceStr);
                            total += price * item.quantity;
                        } catch (NumberFormatException e) {
                            // Handle parsing error if price format is unexpected
                        }
                    }

                    tvTotalPrice.setText("₹" + total);
                    adapter.notifyDataSetChanged();
                });
    }

    // ==================================================
    // CHECKOUT LOGIC (REDIRECT TO CHECKOUT ACTIVITY)
    // ==================================================
    private void checkoutOrder() {
        int totalAmt = 0;

        // Recalculate total just to be safe
        for (CartFirebaseModel item : cartList) {
            String priceStr = item.price.replace("₹", "").split("/")[0].trim();
            try {
                totalAmt += Integer.parseInt(priceStr) * item.quantity;
            } catch (Exception e) {}
        }

        // Open Checkout Activity
        Intent intent = new Intent(getContext(), CheckoutActivity.class);
        intent.putParcelableArrayListExtra("orderItems", cartList); // Pass Cart Items
        intent.putExtra("totalAmount", (long) totalAmt); // Pass Total
        intent.putExtra("fromCart", true); // Tell Checkout to clear cart after success
        startActivity(intent);
    }
}