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
import com.example.grochub.PreferenceManager;
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
    private ImageView ivProfileImage;
    private View btnAddress, btnOrders, btnHelp, btnWallet, btnChangePassword;
    private MaterialButton btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivProfileImage = view.findViewById(R.id.profile_image);
        btnLogout = view.findViewById(R.id.logout_button);

        btnChangePassword = view.findViewById(R.id.change_password_include);
        btnAddress = view.findViewById(R.id.address_button_include);
        btnOrders = view.findViewById(R.id.order_history_button_include);
        btnWallet = view.findViewById(R.id.wallet_button_include);
        btnHelp = view.findViewById(R.id.help_button_include);

        setupMenuLabels();
        loadUserProfile();
        setupClickListeners();

        return view;
    }

    private void setupMenuLabels() {
        ((TextView) btnChangePassword.findViewById(R.id.menu_title)).setText("Change Password");
        ((ImageView) btnChangePassword.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_lock_idle_lock);

        ((TextView) btnAddress.findViewById(R.id.menu_title)).setText("Delivery Address");
        ((ImageView) btnAddress.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_directions);

        ((TextView) btnOrders.findViewById(R.id.menu_title)).setText("Order History");
        ((ImageView) btnOrders.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_recent_history);

        ((TextView) btnWallet.findViewById(R.id.menu_title)).setText("Wallet & Rewards");
        ((ImageView) btnWallet.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_slideshow);

        ((TextView) btnHelp.findViewById(R.id.menu_title)).setText("Help & Support");
        ((ImageView) btnHelp.findViewById(R.id.menu_icon)).setImageResource(android.R.drawable.ic_menu_help);
    }

    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && user.getProviderData().size() > 1 &&
                    user.getProviderData().get(1).getProviderId().equals("google.com")) {
                Toast.makeText(getContext(), "Manage Google password in settings.", Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
            }
        });

        btnAddress.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddressActivity.class)));
        btnHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));
        btnOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            }
        });

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || !document.exists()) return;
                    String name = document.getString("username");
                    if (name != null) tvName.setText(name);
                    String imageUrl = document.getString("profileImage");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).circleCrop().into(ivProfileImage);
                    }
                });
    }

    private void logoutUser() {
        // ⭐ CLEAR LOCAL PREFS: Ensures re-verification on next login
        PreferenceManager prefManager = new PreferenceManager(requireActivity());
        prefManager.clearPrefs();

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