package com.example.grochub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore; // ⭐ Firestore Import
import com.google.firebase.firestore.SetOptions; // ⭐ Firestore Import

import java.util.HashMap;
import java.util.Map;

public class WelcomePage extends AppCompatActivity {

    private LinearLayout loginButtonContainer;
    private LinearLayout registerButtonContainer;
    private LinearLayout googleButton;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore; // ⭐ Firestore

    private static final int RC_GOOGLE_SIGN_IN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // ⭐ Init Firestore

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        loginButtonContainer = findViewById(R.id.btn_login_container);
        registerButtonContainer = findViewById(R.id.btn_register_container);
        googleButton = findViewById(R.id.btn_social_google);
    }

    private void setupClickListeners() {
        loginButtonContainer.setOnClickListener(v -> startActivity(new Intent(this, Loginpage.class)));
        registerButtonContainer.setOnClickListener(v -> startActivity(new Intent(this, Registerpage.class)));
        googleButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    // ⭐ SAVE USER TO FIRESTORE AFTER LOGIN
                    saveUserToFirestore(user);
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ⭐ Logic to save user to Firestore
    private void saveUserToFirestore(FirebaseUser user) {
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        if (name != null) userData.put("username", name);

        // Use Merge so we don't overwrite existing data (like address)
        firestore.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Even if save fails, let them in, but show error
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}