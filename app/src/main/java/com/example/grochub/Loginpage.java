package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                            // ⭐ CHECK PHONE NUMBER BEFORE MAIN ACTIVITY
                            checkUserAndRedirect();
                        } else {
                            Toast.makeText(this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Login failed",
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

    // ⭐ HELPER METHOD: Decides where to go
    private void checkUserAndRedirect() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // If phone number is missing, force verification
            if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(this, NumberEnter.class);
                startActivity(intent);
            } else {
                // Phone exists, go to Main
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}