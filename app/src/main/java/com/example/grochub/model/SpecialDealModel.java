package com.example.grochub.model;

public class SpecialDealModel {

    private String name, unit, image;
    private int price, specialPrice, offerPercent;
    private boolean isSpecial;

    public SpecialDealModel() {}

    public String getName() { return name; }
    public String getUnit() { return unit; }
    public String getImage() { return image; }

    public int getPrice() { return price; }
    public int getSpecialPrice() { return specialPrice; }
    public int getOfferPercent() { return offerPercent; }

    public boolean isSpecial() { return isSpecial; }
}
