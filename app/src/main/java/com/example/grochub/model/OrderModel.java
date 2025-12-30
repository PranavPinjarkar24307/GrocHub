package com.example.grochub.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class OrderModel {

    // Must match Firestore field names exacty
    public List<CartFirebaseModel> items;
    public int totalPrice;
    public Timestamp createdAt;
    public String status;
    public String orderId;

    public OrderModel() {} // Empty constructor required

    public OrderModel(List<CartFirebaseModel> items, int totalPrice, Timestamp createdAt, String status) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.status = status;
    }
}