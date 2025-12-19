package com.example.grochub.model;

public class ProductModel {

    private String name;
    private long price;      // price from Firestore
    private String image;
    private String category;

    // 🔒 PERMANENT FIRESTORE DOCUMENT ID (e.g. "tomato")
    private String id;

    private String unit;

    // REQUIRED empty constructor for Firestore
    public ProductModel() {
    }

    // ================= GETTERS =================

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public String getId() {
        return id;
    }

    // ================= SETTER (ONLY REQUIRED FIX) =================
    // 🔥 This allows Firestore document ID to be injected safely
    public void setId(String id) {
        this.id = id;
    }
}
