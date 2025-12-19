package com.example.grochub.model;

public class SpecialDealModel {

    // 🔥 Firestore document ID (PERMANENT like "tomato")
    private String id;

    private String name;
    private String unit;
    private String image;

    private int price;
    private int specialPrice;
    private int offerPercent;
    private boolean isSpecial;

    // 🔹 Required empty constructor for Firestore
    public SpecialDealModel() {}

    // ---------- ID ----------
    public String getId() {
        return id;
    }

    // 🔥 YOU WILL SET THIS MANUALLY FROM doc.getId()
    public void setId(String id) {
        this.id = id;
    }

    // ---------- TEXT ----------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // ---------- PRICES ----------
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(int specialPrice) {
        this.specialPrice = specialPrice;
    }

    public int getOfferPercent() {
        return offerPercent;
    }

    public void setOfferPercent(int offerPercent) {
        this.offerPercent = offerPercent;
    }

    // ---------- FLAGS ----------
    public boolean isSpecial() {
        return isSpecial;
    }

    public void setSpecial(boolean special) {
        isSpecial = special;
    }
}
