package com.example.grochub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_IMAGE = "extra_image";

    private ImageView ivProductImage, ivBack, ivWishlist;
    private TextView tvName, tvPrice, tvDescription, tvQty, btnQtyMinus, btnQtyPlus;

    private int quantity = 1;
    private boolean inWishlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ivProductImage = findViewById(R.id.iv_product_image);
        ivBack = findViewById(R.id.iv_back);
        ivWishlist = findViewById(R.id.iv_wishlist);

        tvName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_product_price);
        tvDescription = findViewById(R.id.tv_product_description);
        tvQty = findViewById(R.id.tv_qty);
        btnQtyMinus = findViewById(R.id.btn_qty_minus);
        btnQtyPlus = findViewById(R.id.btn_qty_plus);

        // get data from Intent
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String price = getIntent().getStringExtra(EXTRA_PRICE);
        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE, 0);

        if (name != null) tvName.setText(name);
        if (price != null) tvPrice.setText(price);
        if (imageRes != 0) ivProductImage.setImageResource(imageRes);

        // description: later you can pass real description
        tvDescription.setText("Fresh and high-quality " + name + " delivered to your doorstep.");

        ivBack.setOnClickListener(v -> onBackPressed());

        ivWishlist.setOnClickListener(v -> {
            inWishlist = !inWishlist;
            ivWishlist.setImageResource(
                    inWishlist ? R.drawable.hearticon : R.drawable.wishlisticon
            );
            // later: add / remove from wishlist database here
        });

        btnQtyMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQty.setText(String.valueOf(quantity));
            }
        });

        btnQtyPlus.setOnClickListener(v -> {
            quantity++;
            tvQty.setText(String.valueOf(quantity));
        });

        // TODO: Add real logic for Add to Cart / Buy Now
        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> {
            // later: add product with [name, price, imageRes, quantity] to Cart DB / Singleton
        });

        findViewById(R.id.btn_buy_now).setOnClickListener(v -> {
            // later: go to checkout screen directly
        });
    }
}
