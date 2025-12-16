package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Registerpage extends AppCompatActivity {

    private EditText etEmail, etUsername;
    private LinearLayout btnRegister;
    private TextView tvLoginLink;

    public static final String EXTRA_EMAIL = "com.example.grochub.EMAIL";
    public static final String EXTRA_USERNAME = "com.example.grochub.USERNAME";

    // Constants for username validation
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    // Regex for alphanumeric and underscore characters
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registerpage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Email and Username are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Username Validation Starts Here ---

            if (username.length() < MIN_USERNAME_LENGTH) {
                Toast.makeText(this, "Username must be at least " + MIN_USERNAME_LENGTH + " characters long", Toast.LENGTH_LONG).show();
                return;
            }

            if (username.length() > MAX_USERNAME_LENGTH) {
                Toast.makeText(this, "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters", Toast.LENGTH_LONG).show();
                return;
            }

            if (!username.matches(USERNAME_PATTERN)) {
                Toast.makeText(this, "Username can only contain letters, numbers, and underscores", Toast.LENGTH_LONG).show();
                return;
            }

            // --- Username Validation Ends Here ---

            Intent intent = new Intent(this, RegisterPagePassword.class);
            intent.putExtra(EXTRA_EMAIL, email);
            intent.putExtra(EXTRA_USERNAME, username);
            startActivity(intent);
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, Loginpage.class));
            finish();
        });
    }
}