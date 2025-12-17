package com.example.grochub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.grochub.Loginpage;
import com.example.grochub.MainActivity;
import com.example.grochub.R;
import com.example.grochub.WelcomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private View orderHistoryButton;
    private Button logoutButton;
    private TextView tvName, tvEmail;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI
        orderHistoryButton = view.findViewById(R.id.order_history_button);
        logoutButton = view.findViewById(R.id.logout_button);
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);

        // Load profile data
        loadUserProfile();

        // Order History
        orderHistoryButton.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openOrderHistory()
        );

        // 🔥 LOGOUT CLICK (THIS WAS MISSING)
        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvEmail.setText(user.getEmail());

            if (user.getDisplayName() != null) {
                tvName.setText(user.getDisplayName());
            } else {
                tvName.setText("User");
            }
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(requireActivity(), WelcomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }

}
