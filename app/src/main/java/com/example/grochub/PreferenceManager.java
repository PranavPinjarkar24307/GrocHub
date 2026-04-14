package com.example.grochub;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "GrocHubPrefs";
    private static final String KEY_IS_PHONE_VERIFIED = "is_phone_verified_locally";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setPhoneVerifiedLocally(boolean isVerified) {
        editor.putBoolean(KEY_IS_PHONE_VERIFIED, isVerified);
        editor.apply();
    }

    public boolean isPhoneVerifiedLocally() {
        return pref.getBoolean(KEY_IS_PHONE_VERIFIED, false);
    }

    public void clearPrefs() {
        editor.clear();
        editor.apply();
    }
}