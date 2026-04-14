package com.example.grochub;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPass, etNewPass, etConfirmPass;
    private ImageView ivToggleCurrent, ivToggleNew, ivToggleConfirm;
    private LinearLayout btnUpdate;
    private TextView tvBtnText;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Bind Views
        etCurrentPass = findViewById(R.id.et_current_password);
        etNewPass = findViewById(R.id.et_new_password);
        etConfirmPass = findViewById(R.id.et_confirm_password);

        ivToggleCurrent = findViewById(R.id.iv_toggle_current);
        ivToggleNew = findViewById(R.id.iv_toggle_new);
        ivToggleConfirm = findViewById(R.id.iv_toggle_confirm);

        btnUpdate = findViewById(R.id.btn_update_password);
        tvBtnText = findViewById(R.id.tv_btn_text);

        // Back Button
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // Setup Visibility Toggles
        setupPasswordToggle(etCurrentPass, ivToggleCurrent);
        setupPasswordToggle(etNewPass, ivToggleNew);
        setupPasswordToggle(etConfirmPass, ivToggleConfirm);

        btnUpdate.setOnClickListener(v -> performPasswordChange());
    }

    private void setupPasswordToggle(EditText editText, ImageView toggleIcon) {
        toggleIcon.setOnClickListener(v -> {
            if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                // Show Password
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleIcon.setImageResource(R.drawable.ic_eye_open);
            } else {
                // Hide Password
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleIcon.setImageResource(R.drawable.ic_eye_closed);
            }
            editText.setSelection(editText.getText().length());
        });
    }

    private void performPasswordChange() {
        String currentPass = etCurrentPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // 1. Validation
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.equals(currentPass)) {
            Toast.makeText(this, "New password cannot be same as old one", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // 2. Re-authenticate (Required by Firebase for security-sensitive changes)
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 3. Update Password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    setLoading(false);
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Update failed: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                setLoading(false);
                Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnUpdate.setEnabled(!isLoading);
        tvBtnText.setText(isLoading ? "Updating..." : "Update Password");
        btnUpdate.setAlpha(isLoading ? 0.5f : 1.0f);
        etCurrentPass.setEnabled(!isLoading);
        etNewPass.setEnabled(!isLoading);
        etConfirmPass.setEnabled(!isLoading);
    }
}