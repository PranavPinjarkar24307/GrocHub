package com.example.grochub.model;

import com.google.firebase.Timestamp;

public class WishlistItem {

    // 🔥 MUST BE PUBLIC (Firestore requirement)
    public String productId;
    public String name;
    public long price;
    public long specialPrice;
    public String unit;
    public String image;
    public Timestamp addedAt;

    // 🔥 REQUIRED empty constructor
    public WishlistItem() {}

    public WishlistItem(
            String productId,
            String name,
            long price,
            long specialPrice,
            String unit,
            String image
    ) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.specialPrice = specialPrice;
        this.unit = unit;
        this.image = image;
        this.addedAt = Timestamp.now();
    }
}
