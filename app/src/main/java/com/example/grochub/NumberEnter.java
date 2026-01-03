package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class NumberEnter extends AppCompatActivity {

    private EditText etPhone;
    private LinearLayout btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_enter);

        etPhone = findViewById(R.id.et_phone_number);
        btnNext = findViewById(R.id.btn_next);

        // ⭐ DISABLED FOR TESTING:
        // If you want to force number entry testing, keep this commented out.
        // Uncomment it later when the app is ready for production.

        /* if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        */

        btnNext.setOnClickListener(v -> {
            String number = etPhone.getText().toString().trim();

            if (number.isEmpty() || number.length() < 10) {
                etPhone.setError("Enter a valid 10-digit number");
                return;
            }

            // Force +91 for India
            String fullNumber;
            if (!number.startsWith("+")) {
                fullNumber = "+91" + number;
            } else {
                fullNumber = number;
            }

            Intent intent = new Intent(NumberEnter.this, NumberEnterVerification.class);
            intent.putExtra("phonenumber", fullNumber);
            startActivity(intent);
        });

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }
}