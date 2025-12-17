package com.example.grochub.fragment;

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

import com.example.grochub.R;
import com.example.grochub.adapter.CartAdapter;
import com.example.grochub.model.CartFirebaseModel;
import com.example.grochub.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private View btnCheckout;

    private CartAdapter adapter;
    private final List<CartFirebaseModel> cartList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
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

    // ================================
    // LOAD CART FROM FIREBASE
    // ================================
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

                        String priceStr = item.price
                                .replace("₹", "")
                                .split("/")[0]
                                .trim();

                        int price = Integer.parseInt(priceStr);
                        total += price * item.quantity;
                    }

                    tvTotalPrice.setText("₹" + total);
                    adapter.notifyDataSetChanged();
                });
    }

    // ================================
    // CHECKOUT → CREATE ORDER
    // ================================
    private void checkoutOrder() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String orderId = "order_" + System.currentTimeMillis();

        OrderModel order = new OrderModel(
                new ArrayList<>(cartList),
                calculateTotal(cartList),
                null,            // Firestore timestamp
                "PROCESSING"     // ✅ order status
        );

        db.collection("users")
                .document(uid)
                .collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(unused -> {
                    clearFirebaseCart();
                    Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Order failed", Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // CALCULATE TOTAL
    // ================================
    private int calculateTotal(List<CartFirebaseModel> items) {

        int total = 0;

        for (CartFirebaseModel item : items) {
            String priceStr = item.price
                    .replace("₹", "")
                    .split("/")[0]
                    .trim();

            int price = Integer.parseInt(priceStr);
            total += price * item.quantity;
        }

        return total;
    }

    // ================================
    // CLEAR CART AFTER ORDER
    // ================================
    private void clearFirebaseCart() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    OrderModel order = new OrderModel(
            new ArrayList<>(cartList),
            calculateTotal(cartList),
            null,              // Firestore timestamp
            "PROCESSING"       // ✅ DEFAULT STATUS
    );

}
