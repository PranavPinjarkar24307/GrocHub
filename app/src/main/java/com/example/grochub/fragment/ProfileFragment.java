package com.example.grochub.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.grochub.MainActivity;
import com.example.grochub.R;
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

        // 🔥 LOAD PROFILE DATA AGAIN (IMPORTANT)
        loadUserProfile();

        // Navigation
        orderHistoryButton.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openOrderHistory()
        );

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
}
