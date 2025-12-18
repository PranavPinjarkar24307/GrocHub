package com.example.grochub.model;

public class HomeSliderModel {

    private String imageUrl;
    private boolean active;
    private int order;

    public HomeSliderModel() {
        // Required empty constructor for Firebase
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public int getOrder() {
        return order;
    }
}
