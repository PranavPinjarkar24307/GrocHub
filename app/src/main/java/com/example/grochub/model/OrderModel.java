package com.example.grochub.model;

import java.util.List;

public class OrderModel {

    public List<CartFirebaseModel> items;
    public int totalAmount;
    public String status;
    public Object createdAt;

    public OrderModel() {}

    public OrderModel(List<CartFirebaseModel> items,
                      int totalAmount,
                      Object createdAt) {
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = "PROCESSING";
        this.createdAt = createdAt;
    }
}
