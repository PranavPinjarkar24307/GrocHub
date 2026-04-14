package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private TextView tvDisplayNumber, tvTimer;
    private EditText etCodeInput;
    private LinearLayout btnVerify;
    private TextView btnText;

    private CountDownTimer countDownTimer;
    private boolean canResend = false;
    private boolean isVerifying = false; // ⭐ Safety Flag

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
        btnText = findViewById(R.id.btn_next_text);
        tvTimer = findViewById(R.id.tv_timer);

        formatDisplayNumber();
        setupAutoSubmit();
        sendVerificationCode(phoneNumber, false);

        btnVerify.setOnClickListener(v -> handleManualVerify());

        findViewById(R.id.r4ydvwq8ilww).setOnClickListener(v -> {
            mAuth.signOut();
            finish();
        });

        tvTimer.setOnClickListener(v -> {
            if (canResend) {
                sendVerificationCode(phoneNumber, true);
            }
        });
    }

    private void formatDisplayNumber() {
        if (phoneNumber != null && phoneNumber.startsWith("+91")) {
            tvDisplayNumber.setText("+91 " + phoneNumber.substring(3));
        } else {
            tvDisplayNumber.setText(phoneNumber);
        }
    }

    private void startResendTimer() {
        canResend = false;
        tvTimer.setTextColor(getResources().getColor(android.R.color.darker_gray));
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Resend code in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Resend OTP Now");
                tvTimer.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                canResend = true;
            }
        }.start();
    }

    private void setupAutoSubmit() {
        etCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6 && !isVerifying) {
                    verifyCode(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleManualVerify() {
        String code = etCodeInput.getText().toString().trim();
        if (code.length() < 6) {
            Toast.makeText(this, "Enter 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isVerifying) verifyCode(code);
    }

    private void setInProgress(boolean loading) {
        isVerifying = loading;
        btnVerify.setEnabled(!loading);
        btnText.setText(loading ? "Verifying..." : "Next");
        btnVerify.setAlpha(loading ? 0.5f : 1.0f);
        etCodeInput.setEnabled(!loading);
    }

    private void sendVerificationCode(String phone, boolean isResend) {
        PhoneAuthOptions.Builder optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks);

        if (isResend && mResendToken != null) {
            optionsBuilder.setForceResendingToken(mResendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build());
        startResendTimer();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        etCodeInput.setText(code);
                        if (!isVerifying) verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    setInProgress(false);
                    Toast.makeText(NumberEnterVerification.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    mVerificationId = verificationId;
                    mResendToken = token;
                    Toast.makeText(NumberEnterVerification.this, "OTP Sent!", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        if (mVerificationId == null || isVerifying) return;
        setInProgress(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        linkPhoneWithAccount(credential);
    }

    private void linkPhoneWithAccount(PhoneAuthCredential credential) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            setInProgress(false);
            return;
        }

        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        savePhoneToFirestore(currentUser.getUid());
                    } else {
                        // ⭐ CRITICAL FIX: Handle if phone is already used by another account
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.w("OTP", "Collision detected, overriding with Firestore update.");
                            savePhoneToFirestore(currentUser.getUid());
                        } else {
                            setInProgress(false);
                            Toast.makeText(this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void savePhoneToFirestore(String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phoneNumber);

        firestore.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    PreferenceManager prefManager = new PreferenceManager(this);
                    prefManager.setPhoneVerifiedLocally(true);

                    Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}