package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot; // Required
import com.google.firebase.firestore.FirebaseFirestore; // Required

public class Loginpage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout btnLogin;
    private FirebaseAuth auth;
    private ImageView ivPasswordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        // ... (Window Insets code same as before) ...
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        ivPasswordToggle = findViewById(R.id.iv_password_toggle);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password.", Toast.LENGTH_LONG).show();
                return;
            }

            btnLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {
                            // ⭐ CHANGED: Use Firestore check instead of Auth check
                            checkUserAndRedirect();
                        } else {
                            Toast.makeText(this,
                                    task.getException() != null ? task.getException().getMessage() : "Login failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        ivPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_closed);
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    // ==================================================
    // 🔥 PROFESSIONAL DATABASE CHECK
    // ==================================================
    private void checkUserAndRedirect() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Check FIRESTORE specifically for the 'phone' field
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    // 1. Check if user document exists
                    // 2. Check if 'phone' field exists and is not empty
                    boolean hasPhoneInDb = documentSnapshot.exists()
                            && documentSnapshot.contains("phone")
                            && documentSnapshot.getString("phone") != null
                            && !documentSnapshot.getString("phone").isEmpty();

                    if (hasPhoneInDb) {
                        // Phone verified & Saved -> Go to Main
                        Intent intent = new Intent(Loginpage.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Phone missing -> Go to Verification
                        Intent intent = new Intent(Loginpage.this, NumberEnter.class);
                        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Optional: prevents back button
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Loginpage.this, "Error checking profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}