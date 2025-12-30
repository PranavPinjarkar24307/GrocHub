package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class DummyPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_payment);

        String paymentMethod = getIntent().getStringExtra("paymentMethod");

        // Simulate Flipkart/Blinkit payment delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("paymentSuccess", true);
            resultIntent.putExtra("paymentMethod", paymentMethod);
            setResult(RESULT_OK, resultIntent);
            finish();
        }, paymentMethod.equals("UPI") ? 2500 : 1200);
    }
}
