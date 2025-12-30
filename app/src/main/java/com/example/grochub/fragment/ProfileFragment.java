package com.example.grochub.fragment;

import android.content.Intent;
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

public class ProfileFragment extends Fragment {

    // ================= UI =================
    private TextView tvName, tvEmail;
    private ImageView ivProfileImage, ivSettings;
    private View btnAddress, btnOrders, btnHelp;
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

        // 1️⃣ Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2️⃣ View binding (must match XML ids)
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivProfileImage = view.findViewById(R.id.profile_image);
        ivSettings = view.findViewById(R.id.iv_settings);

        btnAddress = view.findViewById(R.id.address_button);
        btnOrders = view.findViewById(R.id.order_history_button);
        btnHelp = view.findViewById(R.id.help_button); // optional
        btnLogout = view.findViewById(R.id.logout_button);

        // 3️⃣ Load profile data
        loadUserProfile();

        // 4️⃣ Click listeners
        setupClickListeners();

        return view;
    }

    // ================= CLICK LISTENERS =================
    private void setupClickListeners() {

        // Address
        btnAddress.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddressActivity.class)));

        // Order History
        btnOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            } else {
                Toast.makeText(getContext(), "Order history not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Settings / Edit Profile
        ivSettings.setOnClickListener(v ->
                        Toast.makeText(getContext(), "Edit Profile (Coming Soon)", Toast.LENGTH_SHORT).show()
                // startActivity(new Intent(getActivity(), EditProfileActivity.class))
        );

        // Help & Support Button Logic
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                // This opens the static About/Help screen you created earlier
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            });
        }

        // Logout
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // ================= LOAD USER =================
    private void loadUserProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Fast load from Auth
        if (user.getEmail() != null) tvEmail.setText(user.getEmail());
        if (user.getDisplayName() != null) tvName.setText(user.getDisplayName());

        // Full load from Firestore
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || !document.exists()) return;

                    String name = document.getString("name");
                    String email = document.getString("email");
                    String imageUrl = document.getString("profileImage");

                    if (name != null && !name.isEmpty()) tvName.setText(name);
                    if (email != null && !email.isEmpty()) tvEmail.setText(email);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .error(android.R.drawable.sym_def_app_icon)
                                .into(ivProfileImage);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ProfileFragment", "Failed to load user", e));
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
}
