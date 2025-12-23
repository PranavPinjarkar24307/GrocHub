package com.example.grochub.model;

public class PopularItemModel {


    private String id;

    public String name;
    public String unit;
    public String image;
    public int price;

    public PopularItemModel() {} // REQUIRED for Firebase


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public PopularItemModel(String name, String unit, String image, int price) {
        this.name = name;
        this.unit = unit;
        this.image = image;
        this.price = price;
    }
}
