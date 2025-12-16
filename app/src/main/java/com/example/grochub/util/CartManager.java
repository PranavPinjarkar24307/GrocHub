package com.example.grochub.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.grochub.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final String PREF_NAME = "cart_pref";
    private static final String CART_KEY = "cart_items";

    public static void addToCart(Context context, CartItem item) {

        List<CartItem> cartList = getCart(context);
        cartList.add(item);

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CART_KEY, new Gson().toJson(cartList));
        editor.apply();
    }

    public static List<CartItem> getCart(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = prefs.getString(CART_KEY, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<CartItem>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void clearCart(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(CART_KEY)
                .apply();
    }
}
