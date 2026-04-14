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

        // Back behavior
        findViewById(R.id.iv_back).setOnClickListener(v -> handleExit());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });

        btnNext.setOnClickListener(v -> {
            String number = etPhone.getText().toString().trim();

            if (number.length() != 10) {
                etPhone.setError("Please enter a valid 10-digit number");
                return;
            }

            // UI has static +91, so we just append it here
            String fullNumber = "+91" + number;

            Intent intent = new Intent(NumberEnter.this, NumberEnterVerification.class);
            intent.putExtra("phonenumber", fullNumber);
            startActivity(intent);
        });
    }

    private void handleExit() {
        // If user backs out of phone entry, sign them out to prevent unfinished profile state
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, WelcomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}