package com.example.grochub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splashscreen extends AppCompatActivity {

    private static final String TAG = "Splashscreen";
    private static final long MAX_SPLASH_TIME = 5000;

    private boolean navigated = false;
    private FirebaseAuth auth;
    private VideoView videoView;

    private final Handler fallbackHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Keep screen ON during splash
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        auth = FirebaseAuth.getInstance();
        videoView = findViewById(R.id.splashVideo);

        Uri videoUri = Uri.parse(
                "android.resource://" + getPackageName() + "/" + R.raw.splashscreen
        );
        videoView.setVideoURI(videoUri);

        // Absolute safety fallback
        fallbackHandler.postDelayed(this::navigateNext, MAX_SPLASH_TIME);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> navigateNext());

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Video error: " + what + " / " + extra);
            navigateNext();
            return true;
        });
    }

    private void navigateNext() {
        if (navigated) return;
        navigated = true;

        fallbackHandler.removeCallbacksAndMessages(null);

        FirebaseUser currentUser = auth.getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, WelcomePage.class);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        fallbackHandler.removeCallbacksAndMessages(null);
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }
}
