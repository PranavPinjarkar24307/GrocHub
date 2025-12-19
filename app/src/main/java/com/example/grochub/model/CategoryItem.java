package com.example.grochub.model;

public class CategoryItem {

    private String id;
    private String name;
    private long price;          // original price
    private long specialPrice;   // 🔥 ADD THIS
    private String unit;
    private String categoryId;
    private String image;

    public CategoryItem() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public long getSpecialPrice() {
        return specialPrice;
    }

    public String getUnit() {
        return unit;
    }

    public String getImage() {
        return image;
    }
}
