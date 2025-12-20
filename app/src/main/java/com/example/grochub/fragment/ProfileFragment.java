package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.grochub.AddressActivity;
import com.example.grochub.MainActivity;
import com.example.grochub.R;
import com.example.grochub.WelcomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private View orderHistoryButton;
    private View addressButton; // We will link this to the clickable text
    private Button logoutButton;
    private TextView tvName, tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Initialize Views
        orderHistoryButton = view.findViewById(R.id.order_history_button);

        // ⭐ FIX: We target the TextView ID 'address_button' instead of the card
        addressButton = view.findViewById(R.id.address_button);

        logoutButton = view.findViewById(R.id.logout_button);
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);

        loadUserProfile();

        // 2. Setup Address Click Listener
        addressButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), AddressActivity.class);
                startActivity(intent);
            }
        });

        // 3. Other Listeners
        orderHistoryButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).openOrderHistory();
            }
        });

        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        }
    }

    private void logoutUser() {
        if (getActivity() == null) return;
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), WelcomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}