package com.example.grochub.model;

public class CategoryItem {
    private int imageResId;
    private String name;
    private String price;

    public CategoryItem(int imageResId, String name, String price) {
        this.imageResId = imageResId;
        this.name = name;
        this.price = price;
    }

    public int getImageResId() { return imageResId; }
    public String getName() { return name; }
    public String getPrice() { return price; }
}
