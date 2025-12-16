package com.example.grochub.model;

public class CartItem {

    private String name;
    private String price;
    private String image;
    private int quantity;

    public CartItem(String name, String price, String image, int quantity) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public int getQuantity() {
        return quantity;
    }
}
