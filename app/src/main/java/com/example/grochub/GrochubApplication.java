package com.example.grochub;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class GrochubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 💾 ENABLE OFFLINE PERSISTENCE
        // This makes the app remember data even after it is closed/killed
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}