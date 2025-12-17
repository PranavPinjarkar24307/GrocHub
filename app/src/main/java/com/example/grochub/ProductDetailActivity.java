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

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage, ivBack, ivWishlist;
    private TextView tvName, tvPrice, tvDescription, tvQty, btnQtyMinus, btnQtyPlus;
    private TextView btnAddToCart;

    private int quantity = 1;
    private boolean inWishlist = false;

    private String imageUrl;

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

        // ✅ CORRECT INTENT KEYS (MATCH ADAPTER)
        String name = getIntent().getStringExtra("product_name");
        long price = getIntent().getLongExtra("product_price", 0);
        imageUrl = getIntent().getStringExtra("product_image");

        // ✅ SET UI
        tvName.setText(name != null ? name : "");
        tvPrice.setText("₹" + price);
        tvQty.setText(String.valueOf(quantity));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_colour)
                    .into(ivProductImage);
        }

        tvDescription.setText(
                "Fresh and high-quality " + (name != null ? name : "product")
                        + " delivered to your doorstep."
        );

        // 🔙 Back
        ivBack.setOnClickListener(v -> onBackPressed());

        // ❤️ Wishlist
        ivWishlist.setOnClickListener(v -> {
            inWishlist = !inWishlist;
            ivWishlist.setImageResource(
                    inWishlist ? R.drawable.hearticon : R.drawable.wishlisticon
            );
        });

        // ➖ Quantity
        btnQtyMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQty.setText(String.valueOf(quantity));
            }
        });

        // ➕ Quantity
        btnQtyPlus.setOnClickListener(v -> {
            quantity++;
            tvQty.setText(String.valueOf(quantity));
        });

        // 🛒 Add to Cart
        btnAddToCart.setOnClickListener(v -> {

            // Local cart
            CartItem cartItem = new CartItem(
                    tvName.getText().toString(),
                    tvPrice.getText().toString(),
                    imageUrl,
                    quantity
            );
            CartManager.addToCart(this, cartItem);

            // Firebase cart
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    }
}
