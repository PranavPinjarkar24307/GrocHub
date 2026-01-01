package com.example.grochub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AddressActivity
 * * Handles the user interface for viewing and editing the shipping/billing address.
 * Features:
 * 1. Loads existing address from Firebase Firestore.
 * 2. Fetches current device location using GPS and autofills fields via Geocoder.
 * 3. Saves updated address details back to Firestore.
 */
public class AddressActivity extends AppCompatActivity {

    // UI Components for address input
    // Note: etPhone was removed as per requirements
    private TextInputEditText etAddressLine, etCity, etState, etPinCode;

    // Buttons for actions
    private View btnCurrentLocation; // Using View allows flexibility (could be a layout or button)
    private Button btnSave;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Firebase Instance
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Bind UI views to their XML IDs
        etAddressLine = findViewById(R.id.et_address_line);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etPinCode = findViewById(R.id.et_pin_code);

        btnCurrentLocation = findViewById(R.id.btn_current_location);
        btnSave = findViewById(R.id.btn_save_address);

        // Initialize the Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Attempt to pre-fill fields with data from Firestore if it exists
        loadSavedAddress();

        // Set up click listeners
        btnCurrentLocation.setOnClickListener(v -> checkPermissionAndGetLocation());
        btnSave.setOnClickListener(v -> saveAddressToFirestore());
    }

    /**
     * Fetches the current user's document from the "users" collection.
     * If an address map exists, it populates the EditText fields.
     */
    private void loadSavedAddress() {
        String uid = FirebaseAuth.getInstance().getUid();

        // Ensure user is logged in
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the nested "address" map from the user document
                            Map<String, Object> addressMap = (Map<String, Object>) documentSnapshot.get("address");

                            // Safely populate fields if data is not null
                            if (addressMap != null) {
                                etAddressLine.setText((String) addressMap.get("addressLine"));
                                etCity.setText((String) addressMap.get("city"));
                                etState.setText((String) addressMap.get("state"));
                                etPinCode.setText((String) addressMap.get("pinCode"));
                            }
                        }
                    }); // You might want to add an .addOnFailureListener here for error handling
        }
    }

    /**
     * Collects input from UI, validates it, and saves it to Firestore.
     * Uses SetOptions.merge() to update only the address field without overwriting other user data.
     */
    private void saveAddressToFirestore() {
        // Extract and trim strings to remove accidental whitespace
        String address = etAddressLine.getText() != null ? etAddressLine.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String state = etState.getText() != null ? etState.getText().toString().trim() : "";
        String pinCode = etPinCode.getText() != null ? etPinCode.getText().toString().trim() : "";

        // Basic Validation: Ensure at least the address line is present
        if (address.isEmpty()) {
            Toast.makeText(this, "Please fill in Address", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        // Structure the data: Create a Map for the address details
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("addressLine", address);
        addressData.put("city", city);
        addressData.put("state", state);
        addressData.put("pinCode", pinCode);

        // Prepare the update map: Key "address" holds the addressData map
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("address", addressData);

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            // Update Firestore
            firestore.collection("users").document(uid)
                    .set(userUpdate, SetOptions.merge()) // MERGE is critical here to preserve other user fields (e.g., email, name)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddressActivity.this, "Address Saved!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity on success
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddressActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    // ------------------------------------------------------------------------
    // Location Logic
    // ------------------------------------------------------------------------

    /**
     * Checks if the ACCESS_FINE_LOCATION permission is granted.
     * If not, requests it. If yes, proceeds to get location.
     */
    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getCurrentLocation();
        }
    }

    /**
     * Uses FusedLocationProviderClient to get the last known location.
     * Note: getLastLocation() can return null if the location has rarely been used recently.
     */
    private void getCurrentLocation() {
        // Double-check permissions (Required by Android Lint even if checked previously)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Location found, proceed to reverse geocoding
                fillAddressForm(location);
            } else {
                Toast.makeText(this, "Ensure GPS is on or try opening Google Maps first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Reverse Geocoding: Converts Latitude/Longitude into a human-readable address.
     * Populates the UI fields with the result.
     * * @param location The Location object containing lat/long.
     */
    private void fillAddressForm(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Get 1 result for the given coordinates
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Populate UI
                etAddressLine.setText(address.getAddressLine(0)); // Full address string
                etCity.setText(address.getLocality());            // City
                etState.setText(address.getAdminArea());          // State/Province
                etPinCode.setText(address.getPostalCode());       // Zip/Pin Code
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get address. Check internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for the result from requesting permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, retry getting location
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}