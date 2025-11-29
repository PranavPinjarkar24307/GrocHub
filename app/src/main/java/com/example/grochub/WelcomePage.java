package com.example.grochub;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for the main Welcome/Login screen.
 * This class links the descriptive IDs from the XML layout
 * and sets up basic click listeners for interaction.
 */
public class WelcomePage extends AppCompatActivity {

    // Define variables for the interactive buttons (LinearLayouts in this case)
    private LinearLayout loginButtonContainer;
    private LinearLayout registerButtonContainer;
    private LinearLayout googleButton;
    private LinearLayout facebookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to your main layout file
        // NOTE: Assuming your layout XML is saved as 'activity_welcome.xml'
        setContentView(R.layout.activity_welcome_page);

        // 1. Initialize views by finding them using their corresponding IDs
        initViews();

        // 2. Set up click handlers for all buttons
        setupClickListeners();
    }

    /**
     * Finds and initializes all necessary UI components using their IDs.
     */
    private void initViews() {
        loginButtonContainer = findViewById(R.id.btn_login_container);
        registerButtonContainer = findViewById(R.id.btn_register_container);
        googleButton = findViewById(R.id.btn_social_google);
        facebookButton = findViewById(R.id.btn_social_facebook);

        // Note: The image view, TextViews, and purely cosmetic layouts usually don't need
        // an explicit variable here unless you plan to modify their content/appearance dynamically.
    }

    /**
     * Sets OnClickListener for all interactive elements.
     */
    private void setupClickListeners() {
        // Click listener for the main Login button
        loginButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would start a LoginActivity here
                Toast.makeText(WelcomePage.this, "Navigating to Login Screen...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WelcomePage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Click listener for the Register button
        registerButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would start a RegisterActivity here
                Toast.makeText(WelcomePage.this, "Navigating to Registration Screen...", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for Google social login
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would initiate Google Sign-In flow
                Toast.makeText(WelcomePage.this, "Starting Google Sign-In process...", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for Facebook social login
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would initiate Facebook Login flow
                Toast.makeText(WelcomePage.this, "Starting Facebook Login process...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}