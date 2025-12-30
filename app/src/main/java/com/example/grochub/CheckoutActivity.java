package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grochub.model.CartFirebaseModel;
import com.example.grochub.model.OrderModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvAddress, tvTotalAmount, tvItemCount;
    private Button btnPlaceOrder;
    private View btnChangeAddress; // Added View type for click listener

    // Data Variables
    private ArrayList<CartFirebaseModel> orderItems;
    private long totalAmount = 0;
    private boolean isFromCart = false;
    private Map<String, Object> userAddressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // 1. Init Views (IDs now exist in XML)
        tvAddress = findViewById(R.id.tv_checkout_address);
        tvTotalAmount = findViewById(R.id.tv_checkout_total); // Fixed in XML
        tvItemCount = findViewById(R.id.tv_checkout_count);   // Fixed in XML
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        btnChangeAddress = findViewById(R.id.btn_change_address);

        // 2. Get Data from Intent
        if (getIntent() != null) {
            orderItems = getIntent().getParcelableArrayListExtra("orderItems");
            totalAmount = getIntent().getLongExtra("totalAmount", 0);
            isFromCart = getIntent().getBooleanExtra("fromCart", false);
        }

        // 3. Validation
        if (orderItems == null || orderItems.isEmpty()) {
            Toast.makeText(this, "Order error: No items found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4. Update UI
        tvTotalAmount.setText("₹" + totalAmount);
        tvItemCount.setText("Items (" + orderItems.size() + ")");

        // 5. Load Address
        fetchUserAddress();

        // 6. Button Logic
        btnChangeAddress.setOnClickListener(v ->
                startActivity(new Intent(this, AddressActivity.class))
        );

        btnPlaceOrder.setOnClickListener(v -> {
            if (userAddressMap == null || userAddressMap.isEmpty()) {
                Toast.makeText(this, "Please add a delivery address", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trigger Payment Simulation
            processPayment();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserAddress();
    }

    private void fetchUserAddress() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("address")) {
                        userAddressMap = (Map<String, Object>) documentSnapshot.get("address");

                        if (userAddressMap != null) {
                            String fullAddress = userAddressMap.get("addressLine") + ", " +
                                    userAddressMap.get("city") + "\n" +
                                    "Ph: " + userAddressMap.get("phone");

                            tvAddress.setText(fullAddress);
                            btnPlaceOrder.setEnabled(true);
                            btnPlaceOrder.setAlpha(1.0f);
                        }
                    } else {
                        tvAddress.setText("No address found. Please add one.");
                        btnPlaceOrder.setEnabled(false);
                        btnPlaceOrder.setAlpha(0.5f); // Visual cue
                    }
                });
    }

    private void processPayment() {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing...");

        // Simulate network delay
        new android.os.Handler().postDelayed(this::placeOrderInFirestore, 1500);
    }

    private void placeOrderInFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // 1. Create Order
        DocumentReference newOrderRef = db.collection("users").document(uid).collection("orders").document();

        OrderModel order = new OrderModel();
        order.items = orderItems;
        order.totalPrice = (int) totalAmount;
        order.createdAt = new Timestamp(new Date());
        order.status = "Placed";

        batch.set(newOrderRef, order);

        // 2. Clear Cart (ONLY if order came from Cart)
        if (isFromCart) {
            for (CartFirebaseModel item : orderItems) {
                // Ensure document ID generation is safe
                String docId = item.name.toLowerCase().replace(" ", "_");
                DocumentReference cartItemRef = db.collection("users").document(uid).collection("cart").document(docId);
                batch.delete(cartItemRef);
            }
        }

        // 3. Commit
        batch.commit().addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
            startActivity(intent);
            finishAffinity();
        }).addOnFailureListener(e -> {
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText("Place Order");
            Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}