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
import com.example.grochub.ChangePasswordActivity;
import com.example.grochub.MainActivity;
import com.example.grochub.R;
import com.example.grochub.WelcomePage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfileImage, ivSettings;
    private View btnAddress, btnOrders, btnHelp, btnWallet, btnPayment, btnDarkMode;
    private MaterialButton btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Init Firebase & Google Sign-In
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // 2. Standard View Binding
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivProfileImage = view.findViewById(R.id.profile_image);
        ivSettings = view.findViewById(R.id.iv_settings);
        btnLogout = view.findViewById(R.id.logout_button);

        // 3. Include-based View Binding (Fixes the FAILED compilation)
        btnAddress = view.findViewById(R.id.address_button_include);
        btnOrders = view.findViewById(R.id.order_history_button_include);
        btnWallet = view.findViewById(R.id.wallet_button_include);
        btnHelp = view.findViewById(R.id.help_button_include);

        // Setup the text and icons for the included layouts
        setupMenuLabels();

        loadUserProfile();
        setupClickListeners();

        return view;
    }

    private void setupMenuLabels() {
        // Delivery Address
        ((TextView) btnAddress.findViewById(R.id.menu_title)).setText("Delivery Address");
        ((ImageView) btnAddress.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_directions);

        // Order History
        ((TextView) btnOrders.findViewById(R.id.menu_title)).setText("Order History");
        ((ImageView) btnOrders.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_recent_history);

        // Help
        ((TextView) btnHelp.findViewById(R.id.menu_title)).setText("Help & Support");
        ((ImageView) btnHelp.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_help);

        // Wallet
        ((TextView) btnWallet.findViewById(R.id.menu_title)).setText("Wallet & Rewards");
        ((ImageView) btnWallet.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_slideshow);
    }

    private void setupClickListeners() {
        btnAddress.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddressActivity.class)));

        btnOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            }
        });

        ivSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        btnHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));

        btnWallet.setOnClickListener(v -> Toast.makeText(getContext(), "Rewards Coming Soon!", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || !document.exists()) return;

                    String name = document.getString("username");
                    String email = document.getString("email");
                    String imageUrl = document.getString("profileImage");

                    if (name != null) tvName.setText(name);
                    if (email != null) tvEmail.setText(email);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(ivProfileImage);
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileFragment", "Load Failed", e));
    }

    private void logoutUser() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            if (!isAdded()) return;
            Intent intent = new Intent(getActivity(), WelcomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}