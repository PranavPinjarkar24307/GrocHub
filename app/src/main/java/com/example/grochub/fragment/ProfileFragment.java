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
    private View btnAddress, btnOrders, btnHelp;
    private MaterialButton btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivProfileImage = view.findViewById(R.id.profile_image);
        ivSettings = view.findViewById(R.id.iv_settings);
        btnAddress = view.findViewById(R.id.address_button);
        btnOrders = view.findViewById(R.id.order_history_button);
        btnHelp = view.findViewById(R.id.help_button);
        btnLogout = view.findViewById(R.id.logout_button);

        loadUserProfile();
        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        btnAddress.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddressActivity.class)));

        btnOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openOrderHistory();
            }
        });

        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));
        }

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || !document.exists()) return;
                    tvName.setText(document.getString("username"));
                    tvEmail.setText(document.getString("email"));
                    String imageUrl = document.getString("profileImage");
                    if (imageUrl != null) {
                        Glide.with(this).load(imageUrl).into(ivProfileImage);
                    }
                });
    }

    private void logoutUser() {
        if (getActivity() == null) return;

        // 1. Sign out from Firebase
        auth.signOut();

        // 2. Sign out from Google (Required to stop auto-login)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // 3. Clear activity stack and go to WelcomePage
            Intent intent = new Intent(getActivity(), WelcomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
    }
}