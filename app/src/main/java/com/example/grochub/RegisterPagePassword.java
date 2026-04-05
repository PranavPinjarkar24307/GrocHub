package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView; // Import ImageView
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPagePassword extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private ImageView ivToggleCreate, ivToggleConfirm; // Declare ImageViews

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String email, username;

    // Track visibility states
    private boolean isCreatePasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get Intent Data
        email = getIntent().getStringExtra(Registerpage.EXTRA_EMAIL);
        username = getIntent().getStringExtra(Registerpage.EXTRA_USERNAME);

        if (email == null || username == null) {
            Toast.makeText(this, "Registration data missing", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Registerpage.class));
            finish();
            return;
        }

        // Bind Views
        etPassword = findViewById(R.id.et_create_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivToggleCreate = findViewById(R.id.iv_toggle_create); // Bind Toggle 1
        ivToggleConfirm = findViewById(R.id.iv_toggle_confirm); // Bind Toggle 2
        LinearLayout btnRegister = findViewById(R.id.btn_register);

        // --- SETUP TOGGLES ---
        setupPasswordToggle(etPassword, ivToggleCreate);
        setupPasswordToggle(etConfirmPassword, ivToggleConfirm);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    // Helper function to handle toggle logic
    private void setupPasswordToggle(EditText editText, ImageView toggleIcon) {
        toggleIcon.setOnClickListener(v -> {
            // Check current transformation method
            if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                // SHOW PASSWORD
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleIcon.setImageResource(R.drawable.ic_eye_open); // Ensure this drawable exists
            } else {
                // HIDE PASSWORD
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleIcon.setImageResource(R.drawable.ic_eye_closed);
            }
            // Move cursor to the end of text
            if (editText.getText() != null) {
                editText.setSelection(editText.getText().length());
            }
        });
    }

    private void registerUser() {
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "User creation failed", Toast.LENGTH_LONG).show();
                            return;
                        }
                        saveUserToFirestore(user.getUid());
                    } else {
                        Toast.makeText(this,
                                task.getException() != null ? task.getException().getMessage() : "Registration failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("email", email);
        userMap.put("username", username);
        userMap.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> navigateToHome())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Account created, profile save failed", Toast.LENGTH_LONG).show();
                    navigateToHome();
                });
    }

    private void navigateToHome() {
        // Send to NumberEnter, NOT MainActivity
        Intent intent = new Intent(this, NumberEnter.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}