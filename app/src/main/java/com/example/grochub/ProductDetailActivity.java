package com.example.grochub;

import android.annotation.SuppressLint;
import android.content.Intent; // Required for Intent
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.grochub.model.CartFirebaseModel;
import com.example.grochub.model.CartItem;
import com.example.grochub.model.WishlistItem;
import com.example.grochub.util.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList; // Required for ArrayList

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage, ivBack, ivWishlist;
    private TextView tvName, tvPrice, tvUnit, tvDescription, tvQty;
    // 🔥 Added btnBuyNow
    private TextView btnQtyMinus, btnQtyPlus, btnAddToCart, btnBuyNow;

    private int quantity = 1;

    private String imageUrl;
    private String productId;
    private String productUnit;

    private long normalPrice = 0;
    private long specialPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // ================= VIEW BINDING =================
        ivProductImage = findViewById(R.id.iv_product_image);
        ivBack = findViewById(R.id.iv_back);
        ivWishlist = findViewById(R.id.iv_wishlist);

        tvName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_product_price);
        tvUnit = findViewById(R.id.tv_product_unit);
        tvDescription = findViewById(R.id.tv_product_description);
        tvQty = findViewById(R.id.tv_qty);

        btnQtyMinus = findViewById(R.id.btn_qty_minus);
        btnQtyPlus = findViewById(R.id.btn_qty_plus);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);

        // 🔥 FIXED: Bind Buy Now Button
        btnBuyNow = findViewById(R.id.btn_buy_now);

        tvQty.setText(String.valueOf(quantity));

        // ================= GET PRODUCT ID =================
        productId = getIntent().getStringExtra("product_id");

        if (productId == null || productId.isEmpty()) {
            loadOldFlowProduct();
        } else {
            loadProductFromFirebase();
        }

        // Listeners
        ivBack.setOnClickListener(v -> onBackPressed());
        ivWishlist.setOnClickListener(v -> toggleWishlist());

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

        btnAddToCart.setOnClickListener(v -> addToCart());

        // 🔥 FIXED: Add Click Listener for Buy Now
        btnBuyNow.setOnClickListener(v -> buyNowProcess());
    }

    // ==================================================
    // 🔥 NEW: BUY NOW LOGIC
    // ==================================================
    private void buyNowProcess() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Loginpage.class));
            return;
        }

        // 1. Calculate Price
        long currentPrice = (specialPrice > 0) ? specialPrice : normalPrice;
        long totalAmt = currentPrice * quantity;

        // 2. Create Single Item List for Checkout
        CartFirebaseModel singleItem = new CartFirebaseModel(
                tvName.getText().toString(),
                "₹" + currentPrice, // Price formatted as string
                imageUrl,
                quantity
        );

        ArrayList<CartFirebaseModel> items = new ArrayList<>();
        items.add(singleItem);

        // 3. Open Checkout Activity
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("orderItems", items); // Passing the list
        intent.putExtra("totalAmount", totalAmt); // Passing total price
        intent.putExtra("fromCart", false); // Important: Don't delete cart after this order
        startActivity(intent);
    }

    // ... (Keep existing methods: getSafeProductId, loadOldFlowProduct, loadProductFromFirebase, etc.) ...

    private String getSafeProductId() {
        return (productId != null && !productId.isEmpty())
                ? productId
                : tvName.getText().toString().toLowerCase().replace(" ", "_");
    }

    @SuppressLint("SetTextI18n")
    private void loadOldFlowProduct() {
        String name = getIntent().getStringExtra("product_name");
        long price = getIntent().getLongExtra("product_price", -1);
        imageUrl = getIntent().getStringExtra("product_image");

        if (name == null || price <= 0) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        normalPrice = price;
        specialPrice = 0;

        tvName.setText(name);
        tvPrice.setText("₹" + price);
        tvUnit.setVisibility(View.GONE);
        tvDescription.setText(buildProductDescription(name, null, normalPrice, specialPrice));
        loadImage(imageUrl);
        checkWishlistState();
    }

    @SuppressLint("SetTextI18n")
    private void loadProductFromFirebase() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("name");
                    Long np = doc.getLong("price");
                    Long sp = doc.getLong("specialPrice");

                    normalPrice = np != null ? np : 0;
                    specialPrice = sp != null ? sp : 0;
                    long displayPrice = specialPrice > 0 ? specialPrice : normalPrice;

                    imageUrl = doc.getString("image");
                    productUnit = doc.getString("unit");

                    tvName.setText(name);
                    tvPrice.setText("₹" + displayPrice);

                    if (productUnit != null && !productUnit.isEmpty()) {
                        tvUnit.setText(productUnit);
                        tvUnit.setVisibility(View.VISIBLE);
                    } else {
                        tvUnit.setVisibility(View.GONE);
                    }
                    tvDescription.setText(buildProductDescription(name, productUnit, normalPrice, specialPrice));
                    loadImage(imageUrl);
                    checkWishlistState();
                });
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.gray_colour).into(ivProductImage);
        }
    }

    private void addToCart() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        CartItem cartItem = new CartItem(
                tvName.getText().toString(),
                tvPrice.getText().toString(),
                imageUrl,
                quantity
        );

        CartManager.addToCart(this, cartItem);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("cart")
                .document(getSafeProductId())
                .set(new CartFirebaseModel(
                        tvName.getText().toString(),
                        tvPrice.getText().toString(),
                        imageUrl,
                        quantity
                ));
        Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
    }

    private void checkWishlistState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ivWishlist.setImageResource(R.drawable.wishlisticon);
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).collection("wishlist").document(getSafeProductId())
                .get().addOnSuccessListener(doc -> ivWishlist.setImageResource(doc.exists() ? R.drawable.hearticon : R.drawable.wishlisticon));
    }

    private void toggleWishlist() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pid = getSafeProductId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("wishlist").document(pid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        db.collection("users").document(uid).collection("wishlist").document(pid).delete();
                        ivWishlist.setImageResource(R.drawable.wishlisticon);
                    } else {
                        db.collection("users").document(uid).collection("wishlist").document(pid)
                                .set(new WishlistItem(pid, tvName.getText().toString(), normalPrice, specialPrice, productUnit, imageUrl));
                        ivWishlist.setImageResource(R.drawable.hearticon);
                    }
                });
    }

    private String buildProductDescription(String name, String unit, long normalPrice, long specialPrice) {
        StringBuilder desc = new StringBuilder();
        desc.append(name).append(" is a premium quality product, carefully selected and packed to ensure freshness.\n\n");
        if (unit != null && !unit.isEmpty()) desc.append("• Pack Size: ").append(unit).append("\n");
        if (specialPrice > 0) desc.append("• Special Offer Price: ₹").append(specialPrice).append("\n");
        else desc.append("• Price: ₹").append(normalPrice).append("\n");
        desc.append("\nOrder now for fast delivery.");
        return desc.toString();
    }
}