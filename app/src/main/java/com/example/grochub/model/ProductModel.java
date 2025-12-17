package com.example.grochub.model;

public class ProductModel {

    private String name;
    private long price;      // 🔥 FIXED (was String)
    private String image;
    private String category;
    private String id;

    private String unit;     // 🔥 ADDED (Firestore has this)

    // REQUIRED empty constructor
    public ProductModel() {
    }

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
}
