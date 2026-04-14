package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Loginpage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout btnLogin;
    private TextView tvForgotPassword, tvLoginText, tvRegisterLink;
    private FirebaseAuth auth;
    private ImageView ivPasswordToggle, ivBack;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        auth = FirebaseAuth.getInstance();

        // 1. Initialize Views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvLoginText = findViewById(R.id.tv_login_btn_text);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegisterLink = findViewById(R.id.tv_register_link);
        ivPasswordToggle = findViewById(R.id.iv_password_toggle);
        ivBack = findViewById(R.id.iv_back);

        // 2. Window Insets for Fullscreen UI
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 3. Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());

        // 4. Forgot Password Click
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // 5. Register Link Click
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(Loginpage.this, Registerpage.class));
        });

        // 6. Back Button
        ivBack.setOnClickListener(v -> finish());

        // 7. Password Toggle
        ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserAndRedirect();
                    } else {
                        setLoadingState(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Enter your email first");
            etEmail.requestFocus();
            return;
        }

        Toast.makeText(this, "Sending reset link...", Toast.LENGTH_SHORT).show();
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Reset link sent! Check your inbox.", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkUserAndRedirect() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    Intent intent;
                    // Gatekeeper: Check if phone exists in Firestore
                    if (doc.exists() && doc.contains("phone") && !doc.getString("phone").isEmpty()) {
                        intent = new Intent(this, MainActivity.class);
                    } else {
                        intent = new Intent(this, NumberEnter.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, NumberEnter.class));
                    finish();
                });
    }

    private void setLoadingState(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setAlpha(isLoading ? 0.5f : 1.0f);
        if (tvLoginText != null) {
            tvLoginText.setText(isLoading ? "Please wait..." : "Login");
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_eye_closed);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_eye_open);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }
}