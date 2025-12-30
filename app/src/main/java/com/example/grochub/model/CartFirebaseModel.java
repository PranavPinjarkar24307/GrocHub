package com.example.grochub.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CartFirebaseModel implements Parcelable {

    public String name;
    public String price;
    public String image;
    public int quantity;

    // Empty constructor for Firestore
    public CartFirebaseModel() {}

    public CartFirebaseModel(String name, String price, String image, int quantity) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
    }

    // --- Parcelable Implementation ---
    protected CartFirebaseModel(Parcel in) {
        name = in.readString();
        price = in.readString();
        image = in.readString();
        quantity = in.readInt();
    }

    public static final Creator<CartFirebaseModel> CREATOR = new Creator<CartFirebaseModel>() {
        @Override
        public CartFirebaseModel createFromParcel(Parcel in) {
            return new CartFirebaseModel(in);
        }

        @Override
        public CartFirebaseModel[] newArray(int size) {
            return new CartFirebaseModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(image);
        dest.writeInt(quantity);
    }
}