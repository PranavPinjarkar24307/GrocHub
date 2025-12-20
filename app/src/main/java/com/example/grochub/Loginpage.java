package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Needed for View.OnClickListener
import android.widget.EditText;
import android.widget.LinearLayout; // Correct type for btnLogin
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;


public class Loginpage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout btnLogin; // Correctly declared as LinearLayout
    private FirebaseAuth auth;
    private ImageView ivPasswordToggle;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize EdgeToEdge and Set Content View
        setContentView(R.layout.activity_loginpage);

        // 2. Resolve 'main' ID issue: Ensure your *root* view in XML has android:id="@+id/main"
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            // Optional: Log an error if the ID is still missing
            // Log.e("Loginpage", "Root view with ID 'main' not found.");
        }

        // 3. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // 4. Initialize UI components with correct types and descriptive IDs
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        // **Crucially, find the LinearLayout and assign it to the LinearLayout variable**
        btnLogin = findViewById(R.id.btn_login);

        // 5. Implement the login logic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password.", Toast.LENGTH_LONG).show();
                return;
            }

            btnLogin.setEnabled(false); // 🔒 lock button

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true); // 🔓 unlock

                        if (task.isSuccessful()) {
                            startActivity(new Intent(Loginpage.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Login failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        ivPasswordToggle = findViewById(R.id.iv_password_toggle);

        ivPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_closed);
            } else {
                etPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

    }
}