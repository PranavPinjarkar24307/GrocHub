package com.example.grochub.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.R;
import com.example.grochub.adapter.OrderAdapter;
import com.example.grochub.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvEmpty;
    private ImageView btnBack;

    private final List<OrderModel> orderList = new ArrayList<>();
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // ===== INIT VIEWS =====
        rvOrders = view.findViewById(R.id.rv_orders);
        tvEmpty = view.findViewById(R.id.tv_empty_orders);
        btnBack = view.findViewById(R.id.btn_back);

        // ===== BACK BUTTON =====
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // ===== RECYCLER VIEW =====
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(orderList);
        rvOrders.setAdapter(adapter);

        loadOrders();

        return view;
    }

    // ===== LOAD ORDERS FROM FIREBASE =====
    private void loadOrders() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("orders")
                .orderBy("createdAt")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    orderList.clear();

                    snapshot.getDocuments().forEach(doc -> {
                        OrderModel order = doc.toObject(OrderModel.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    });

                    tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                });
    }
}
