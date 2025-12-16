package com.example.grochub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.grochub.model.CartFirebaseModel;
import com.example.grochub.model.CartItem;
import com.example.grochub.util.CartManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;


public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_IMAGE = "extra_image";

    private ImageView ivProductImage, ivBack, ivWishlist;
    private TextView tvName, tvPrice, tvDescription, tvQty, btnQtyMinus, btnQtyPlus;

    private int quantity = 1;
    private boolean inWishlist = false;

    private TextView btnAddToCart;


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

        btnAddToCart = findViewById(R.id.btn_add_to_cart);


        // ✅ GET DATA CORRECTLY
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String price = getIntent().getStringExtra(EXTRA_PRICE);
        String imageUrl = getIntent().getStringExtra(ProductDetailActivity.EXTRA_IMAGE);

        if (name != null) tvName.setText(name);
        if (price != null) tvPrice.setText(price);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_colour)
                    .into(ivProductImage);
        }

        btnAddToCart.setOnClickListener(v -> {

            // ---------- LOCAL CART (already working) ----------
            CartItem cartItem = new CartItem(
                    tvName.getText().toString(),
                    tvPrice.getText().toString(),
                    imageUrl,
                    quantity
            );

            CartManager.addToCart(this, cartItem);

            // ---------- FIREBASE CART (NEW) ----------
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // using product name as ID (safe for now)
            String productId = tvName.getText().toString();

            db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .document(productId)
                    .set(new CartFirebaseModel(
                            tvName.getText().toString(),
                            tvPrice.getText().toString(),
                            imageUrl,
                            quantity
                    ));

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });


        tvDescription.setText(
                "Fresh and high-quality " + name + " delivered to your doorstep."
        );

        ivBack.setOnClickListener(v -> onBackPressed());

        ivWishlist.setOnClickListener(v -> {
            inWishlist = !inWishlist;
            ivWishlist.setImageResource(
                    inWishlist ? R.drawable.hearticon : R.drawable.wishlisticon
            );
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
    }
}
