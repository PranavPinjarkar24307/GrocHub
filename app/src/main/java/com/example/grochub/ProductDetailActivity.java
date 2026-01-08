package com.example.grochub;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {

    // ================= UI =================
    private ImageView ivProductImage, ivBack, ivWishlist;
    private TextView tvName, tvPrice, tvUnit, tvDescription, tvQty;
    private TextView btnQtyMinus, btnQtyPlus, btnAddToCart, btnBuyNow;

    // ================= PRODUCT DATA =================
    private String productId;
    private String imageUrl;
    private String productUnit;
    private String productCategory;
    private String productType;

    private int quantity = 1;
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
        btnBuyNow = findViewById(R.id.btn_buy_now);

        tvQty.setText(String.valueOf(quantity));

        // ================= LOAD PRODUCT =================
        productId = getIntent().getStringExtra("product_id");

        if (productId == null || productId.isEmpty()) {
            loadOldFlowProduct();
        } else {
            loadProductFromFirebase();
        }

        // ================= LISTENERS =================
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
        btnBuyNow.setOnClickListener(v -> buyNowProcess());
    }

    // ==================================================
    // BUY NOW
    // ==================================================
    private void buyNowProcess() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Loginpage.class));
            return;
        }

        long currentPrice = (specialPrice > 0) ? specialPrice : normalPrice;
        long totalAmount = currentPrice * quantity;

        CartFirebaseModel item = new CartFirebaseModel(
                tvName.getText().toString(),
                "₹" + currentPrice,
                imageUrl,
                quantity
        );

        ArrayList<CartFirebaseModel> items = new ArrayList<>();
        items.add(item);

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("orderItems", items);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("fromCart", false);
        startActivity(intent);
    }

    // ==================================================
    // LOAD OLD FLOW PRODUCT
    // ==================================================
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
        productCategory = "General";
        productType = "Standard Product";

        tvName.setText(name);
        tvPrice.setText("₹" + price);
        tvUnit.setVisibility(View.GONE);

        tvDescription.setText(
                generateProductDescription(
                        name,
                        productCategory,
                        productType,
                        null,
                        price
                )
        );

        loadImage(imageUrl);
        checkWishlistState();
    }

    // ==================================================
    // LOAD PRODUCT FROM FIREBASE
    // ==================================================
    @SuppressLint("SetTextI18n")
    private void loadProductFromFirebase() {

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    productCategory = doc.getString("category");
                    productType = doc.getString("type");

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

                    tvDescription.setText(
                            generateProductDescription(
                                    name,
                                    productCategory,
                                    productType,
                                    productUnit,
                                    displayPrice
                            )
                    );

                    loadImage(imageUrl);
                    checkWishlistState();
                });
    }

    // ==================================================
    // IMAGE
    // ==================================================
    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_colour)
                    .into(ivProductImage);
        }
    }

    // ==================================================
    // CART
    // ==================================================
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

    private String getSafeProductId() {
        return (productId != null && !productId.isEmpty())
                ? productId
                : tvName.getText().toString().toLowerCase().replace(" ", "_");
    }

    // ==================================================
    // WISHLIST
    // ==================================================
    private void checkWishlistState() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ivWishlist.setImageResource(R.drawable.wishlisticon);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("wishlist")
                .document(getSafeProductId())
                .get()
                .addOnSuccessListener(doc ->
                        ivWishlist.setImageResource(
                                doc.exists() ? R.drawable.hearticon : R.drawable.wishlisticon
                        ));
    }

    private void toggleWishlist() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pid = getSafeProductId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .collection("wishlist")
                .document(pid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        db.collection("users").document(uid)
                                .collection("wishlist").document(pid).delete();
                        ivWishlist.setImageResource(R.drawable.wishlisticon);
                    } else {
                        db.collection("users").document(uid)
                                .collection("wishlist").document(pid)
                                .set(new WishlistItem(
                                        pid,
                                        tvName.getText().toString(),
                                        normalPrice,
                                        specialPrice,
                                        productUnit,
                                        imageUrl
                                ));
                        ivWishlist.setImageResource(R.drawable.hearticon);
                    }
                });
    }

    // ==================================================
    // DESCRIPTION GENERATOR (FINAL)
    // ==================================================
    private String generateProductDescription(
            String name,
            String category,
            String type,
            String unit,
            long price
    ) {

        if (category == null) category = "General";
        if (type == null) type = "Standard";

        return "Category: " + category + "\n" +
                "Product Type: " + type + "\n\n" +

                name + " is a high-quality product carefully selected to meet everyday household needs. " +
                "It belongs to the " + category.toLowerCase() + " category and is known for its freshness, consistency, and reliability.\n\n" +

                "Usage & Benefits:\n" +
                getUsageInfo(category) + "\n\n" +

                "Quality Assurance:\n" +
                "✔ Premium quality\n" +
                "✔ Hygienically packed\n" +
                "✔ Suitable for daily use\n" +
                "✔ Best value for money\n\n" +

                "Storage Instructions:\n" +
                getStorageInfo(category) + "\n\n" +

                "Price & Value:\n" +
                "₹" + price + " per " + (unit != null ? unit : "unit") + "\n\n" +

                "Net Quantity: As per selected unit";
    }

    private String getUsageInfo(String category) {

        switch (category.toLowerCase()) {
            case "vegetables":
            case "fruits":
                return "Ideal for daily cooking, salads, juices, and fresh recipes.";
            case "dairy":
                return "Perfect for beverages, cooking, and direct consumption.";
            case "bakery":
                return "Suitable for breakfast, snacks, and quick meals.";
            case "snacks":
            case "packaged food":
                return "Great for quick snacking and on-the-go consumption.";
            default:
                return "Suitable for regular household use.";
        }
    }

    private String getStorageInfo(String category) {

        switch (category.toLowerCase()) {
            case "vegetables":
            case "fruits":
                return "Store in a cool, dry place. Refrigerate for longer freshness.";
            case "dairy":
                return "Keep refrigerated. Consume before expiry.";
            case "bakery":
                return "Store in a cool, dry place.";
            case "snacks":
            case "packaged food":
                return "Store away from heat and sunlight.";
            default:
                return "Store as per package instructions.";
        }
    }
}
