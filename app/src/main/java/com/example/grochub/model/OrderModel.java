package com.example.grochub.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class OrderModel {

    public List<CartFirebaseModel> items;
    public int totalPrice;
    public Timestamp createdAt;

    // 🔥 NEW
    public String status;

    // Required empty constructor
    public OrderModel() {}

    public OrderModel(
            List<CartFirebaseModel> items,
            int totalPrice,
            Timestamp createdAt,
            String status
    ) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.status = status;
    }
}
