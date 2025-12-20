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
import com.google.firebase.firestore.DocumentSnapshot; // ⭐ Firestore Import
import com.google.firebase.firestore.FirebaseFirestore; // ⭐ Firestore Import
import com.google.firebase.firestore.SetOptions; // ⭐ Firestore Import

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddressActivity extends AppCompatActivity {

    private TextInputEditText etAddressLine, etCity, etState, etPinCode, etPhone;
    private View btnCurrentLocation;
    private Button btnSave;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FirebaseFirestore firestore; // ⭐ Firestore Instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Init Firestore
        firestore = FirebaseFirestore.getInstance();

        // Init Views
        etAddressLine = findViewById(R.id.et_address_line);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etPinCode = findViewById(R.id.et_pin_code);
        etPhone = findViewById(R.id.et_phone);
        btnCurrentLocation = findViewById(R.id.btn_current_location);
        btnSave = findViewById(R.id.btn_save_address);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load data from Firestore
        loadSavedAddress();

        btnCurrentLocation.setOnClickListener(v -> checkPermissionAndGetLocation());
        btnSave.setOnClickListener(v -> saveAddressToFirestore());
    }

    private void loadSavedAddress() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            // ⭐ Read from Firestore: users -> [uid]
            firestore.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Check if the "address" field map exists
                            Map<String, Object> addressMap = (Map<String, Object>) documentSnapshot.get("address");
                            if (addressMap != null) {
                                etAddressLine.setText((String) addressMap.get("addressLine"));
                                etCity.setText((String) addressMap.get("city"));
                                etState.setText((String) addressMap.get("state"));
                                etPinCode.setText((String) addressMap.get("pinCode"));
                                etPhone.setText((String) addressMap.get("phone"));
                            }
                        }
                    });
        }
    }

    private void saveAddressToFirestore() {
        String address = etAddressLine.getText() != null ? etAddressLine.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String state = etState.getText() != null ? etState.getText().toString().trim() : "";
        String pinCode = etPinCode.getText() != null ? etPinCode.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in Address and Phone", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        // Create Address Map
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("addressLine", address);
        addressData.put("city", city);
        addressData.put("state", state);
        addressData.put("pinCode", pinCode);
        addressData.put("phone", phone);

        // Create Main Map to update
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("address", addressData); // Save under "address" field

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            // ⭐ Save to Firestore with merge (updates only address, keeps other data)
            firestore.collection("users").document(uid)
                    .set(userUpdate, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddressActivity.this, "Address Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddressActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    // ... (Keep existing permission and location logic unchanged) ...
    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fillAddressForm(location);
            } else {
                Toast.makeText(this, "Ensure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillAddressForm(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etAddressLine.setText(address.getAddressLine(0));
                etCity.setText(address.getLocality());
                etState.setText(address.getAdminArea());
                etPinCode.setText(address.getPostalCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}