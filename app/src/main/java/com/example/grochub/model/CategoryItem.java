package com.example.grochub.model;

public class CategoryItem {

    private String name;
    private int price;
    private String unit;
    private String categoryId;
    private String image;

    public CategoryItem() {}

    public CategoryItem(String image, String name, int price, String unit, String categoryId) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return "₹" + price + " / " + unit;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getImage() {
        return image;
    }
}
