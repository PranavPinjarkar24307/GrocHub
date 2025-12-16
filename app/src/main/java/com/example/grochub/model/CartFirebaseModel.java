package com.example.grochub.model;

import com.google.firebase.firestore.FieldValue;

public class CartFirebaseModel {

    public String name;
    public String price;
    public String image;
    public int quantity;
    public Object timestamp;

    public CartFirebaseModel() {}

    public CartFirebaseModel(String name, String price, String image, int quantity) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.timestamp = FieldValue.serverTimestamp();
    }
}
