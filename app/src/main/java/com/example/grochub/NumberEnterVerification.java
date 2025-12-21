package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NumberEnterVerification extends AppCompatActivity {

    private String mVerificationId;
    private String phoneNumber;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private TextView tvDisplayNumber;
    private EditText etCodeInput;
    private LinearLayout btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_enter_verification);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        phoneNumber = getIntent().getStringExtra("phonenumber");

        tvDisplayNumber = findViewById(R.id.r56362criajk);
        etCodeInput = findViewById(R.id.et_code_input);
        btnVerify = findViewById(R.id.r9p525jbg3i7);

        // ⭐ VISUAL FIX: Add a space after +91 for better look
        // Convert "+9193..." to "+91 93..." just for display
        if (phoneNumber != null && phoneNumber.startsWith("+91")) {
            String formattedNumber = "+91 " + phoneNumber.substring(3);
            tvDisplayNumber.setText(formattedNumber);
        } else {
            tvDisplayNumber.setText(phoneNumber);
        }

        sendVerificationCode(phoneNumber);

        btnVerify.setOnClickListener(v -> {
            String code = etCodeInput.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                Toast.makeText(this, "Enter valid 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyCode(code);
        });

        findViewById(R.id.r4ydvwq8ilww).setOnClickListener(v -> finish());
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        etCodeInput.setText(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // ⭐ LOGGING: Check Logcat for "PhoneAuthError" to see the real reason
                    Log.e("PhoneAuthError", "Verification Failed", e);
                    Toast.makeText(NumberEnterVerification.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    mVerificationId = verificationId;
                    Toast.makeText(NumberEnterVerification.this, "OTP Sent!", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        linkPhoneWithAccount(credential);
    }

    private void linkPhoneWithAccount(PhoneAuthCredential credential) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                savePhoneToFirestore(currentUser.getUid());
                            } else {
                                Toast.makeText(NumberEnterVerification.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void savePhoneToFirestore(String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phoneNumber);

        firestore.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NumberEnterVerification.this, "Verified & Saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(NumberEnterVerification.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    // If save fails, still let user in but log it
                    Log.e("FirestoreError", "Could not save phone: " + e.getMessage());
                    startActivity(new Intent(NumberEnterVerification.this, MainActivity.class));
                    finishAffinity();
                });
    }
}