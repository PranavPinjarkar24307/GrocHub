package com.example.grochub.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.grochub.AboutActivity;
import com.example.grochub.AddressActivity;
import com.example.grochub.MainActivity;
import com.example.grochub.R;
import com.example.grochub.WelcomePage;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    // ================= CONSTANT =================
    private static final int PICK_IMAGE = 101;

    // ================= UI =================
    private TextView tvName, tvEmail;
    private ImageView ivProfileImage, ivSettings;
    private View btnAddress, btnOrders, btnHelp;
    private View btnPaymentMethods, btnWallet, btnDarkMode;
    private MaterialButton btnLogout;

    // ================= FIREBASE =================
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // View binding
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivProfileImage = view.findViewById(R.id.profile_image);
        ivSettings = view.findViewById(R.id.iv_settings);

        btnAddress = view.findViewById(R.id.address_button);
        btnOrders = view.findViewById(R.id.order_history_button);
        btnHelp = view.findViewById(R.id.help_button);
        btnPaymentMethods = view.findViewById(R.id.rg_payment_methods);
        btnWallet = view.findViewById(R.id.wallet_button);
        btnDarkMode = view.findViewById(R.id.dark_mode);
        btnLogout = view.findViewById(R.id.logout_button);

        // Load user
        loadUserProfile();

        // Clicks
        setupClickListeners();

        return view;
    }

    // ================= CLICK LISTENERS =================
    private void setupClickListeners() {

        btnAddress.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddressActivity.class)));

        btnOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            } else {
                Toast.makeText(getContext(), "Order history not available", Toast.LENGTH_SHORT).show();
            }
        });

        ivSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile (Coming Soon)", Toast.LENGTH_SHORT).show()
        );

        if (btnHelp != null) {
            btnHelp.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), AboutActivity.class)));
        }

        if (btnPaymentMethods != null)
            btnPaymentMethods.setOnClickListener(v -> showComingSoon());

        if (btnWallet != null)
            btnWallet.setOnClickListener(v -> showComingSoon());

        if (btnDarkMode != null)
            btnDarkMode.setOnClickListener(v -> showComingSoon());

        // 📸 Change profile photo
        ivProfileImage.setOnClickListener(v -> pickImageFromGallery());

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // ================= LOAD USER =================
    private void loadUserProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        if (user.getEmail() != null) tvEmail.setText(user.getEmail());
        if (user.getDisplayName() != null) tvName.setText(user.getDisplayName());

        // Gmail profile photo
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(ivProfileImage);
        }

        // Firestore custom profile photo (higher priority)
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || !document.exists()) return;

                    String name = document.getString("name");
                    String email = document.getString("email");
                    String imageUrl = document.getString("profileImage");

                    if (name != null) tvName.setText(name);
                    if (email != null) tvEmail.setText(email);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .into(ivProfileImage);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ProfileFragment", "Failed to load profile", e));
    }

    // ================= IMAGE PICK =================
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) uploadProfileImage(imageUri);
        }
    }

    // ================= UPLOAD IMAGE =================
    private void uploadProfileImage(Uri imageUri) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("profile_images")
                .child(uid);

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            db.collection("users").document(uid)
                                    .update("profileImage", uri.toString());

                            Glide.with(this)
                                    .load(uri)
                                    .into(ivProfileImage);

                            Toast.makeText(getContext(),
                                    "Profile photo updated ✅", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    // ================= LOGOUT =================
    private void logoutUser() {
        if (getActivity() == null) return;

        auth.signOut();
        Intent intent = new Intent(getActivity(), WelcomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showComingSoon() {
        Toast.makeText(getContext(), "This feature is coming soon 🚀", Toast.LENGTH_SHORT).show();
    }
}
