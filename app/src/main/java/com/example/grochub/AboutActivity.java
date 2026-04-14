package com.example.grochub; // CHANGE THIS to your actual package name

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This links to your XML file (make sure your XML file is named activity_about.xml)
        setContentView(R.layout.activity_about);

        // Find the back button from the layout
        ImageView backButton = findViewById(R.id.back_button);

        // Make the back button close the screen when clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // This function closes the current activity
            }
        });
    }
}