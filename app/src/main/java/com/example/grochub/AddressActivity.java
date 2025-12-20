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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Init Views
        etAddressLine = findViewById(R.id.et_address_line);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etPinCode = findViewById(R.id.et_pin_code);
        etPhone = findViewById(R.id.et_phone);
        btnCurrentLocation = findViewById(R.id.btn_current_location);
        btnSave = findViewById(R.id.btn_save_address);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔥 LOAD SAVED DATA (Persistence)
        loadSavedAddress();

        btnCurrentLocation.setOnClickListener(v -> checkPermissionAndGetLocation());

        btnSave.setOnClickListener(v -> saveAddressToRealtimeDatabase());
    }

    private void loadSavedAddress() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Address");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String address = snapshot.child("addressLine").getValue(String.class);
                        String city = snapshot.child("city").getValue(String.class);
                        String state = snapshot.child("state").getValue(String.class);
                        String pin = snapshot.child("pinCode").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);

                        if (address != null) etAddressLine.setText(address);
                        if (city != null) etCity.setText(city);
                        if (state != null) etState.setText(state);
                        if (pin != null) etPinCode.setText(pin);
                        if (phone != null) etPhone.setText(phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Do nothing
                }
            });
        }
    }

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
                Toast.makeText(this, "Could not find location. Ensure GPS is on.", Toast.LENGTH_SHORT).show();
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

    private void saveAddressToRealtimeDatabase() {
        String address = etAddressLine.getText() != null ? etAddressLine.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String state = etState.getText() != null ? etState.getText().toString().trim() : "";
        String pinCode = etPinCode.getText() != null ? etPinCode.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in Address and Phone", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loading indicator
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addressLine", address);
        addressMap.put("city", city);
        addressMap.put("state", state);
        addressMap.put("pinCode", pinCode);
        addressMap.put("phone", phone);

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");

            databaseRef.child(uid).child("Address").setValue(addressMap)
                    .addOnSuccessListener(aVoid -> {
                        // ✅ SUCCESS POPUP
                        Toast.makeText(AddressActivity.this, "Address Saved Successfully!", Toast.LENGTH_SHORT).show();
                        // ✅ REDIRECT BACK
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddressActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "Error: User is not logged in!", Toast.LENGTH_SHORT).show();
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