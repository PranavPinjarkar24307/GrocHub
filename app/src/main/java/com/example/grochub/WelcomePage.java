package com.example.grochub;

import android.app.ProgressDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class WelcomePage extends AppCompatActivity {

    private LinearLayout loginButtonContainer, registerButtonContainer, googleButton;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;
    private static final int RC_GOOGLE_SIGN_IN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Syncing with Google...");
        progressDialog.setCancelable(false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButtonContainer = findViewById(R.id.btn_login_container);
        registerButtonContainer = findViewById(R.id.btn_register_container);
        googleButton = findViewById(R.id.btn_social_google);

        loginButtonContainer.setOnClickListener(v -> startActivity(new Intent(this, Loginpage.class)));
        registerButtonContainer.setOnClickListener(v -> startActivity(new Intent(this, Registerpage.class)));

        googleButton.setOnClickListener(v -> {
            progressDialog.show();
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null && !progressDialog.isShowing()) {
            checkUserAndRedirect();
        }
    }

    private void checkUserAndRedirect() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        PreferenceManager prefManager = new PreferenceManager(this);

        // ⭐ MANDATORY LOCAL CHECK (Fails after Uninstall/Clear Data)
        if (!prefManager.isPhoneVerifiedLocally()) {
            startActivity(new Intent(this, NumberEnter.class));
            finish();
            return;
        }

        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();

                    String phone = documentSnapshot.getString("phone");
                    if (documentSnapshot.exists() && phone != null && !phone.trim().isEmpty()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(this, NumberEnter.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    startActivity(new Intent(this, NumberEnter.class));
                    finish();
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
                progressDialog.dismiss();
                Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> saveUserToFirestore(firebaseAuth.getCurrentUser()))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        if (user == null) return;
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("username", user.getDisplayName());

        firestore.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> checkUserAndRedirect());
    }
}