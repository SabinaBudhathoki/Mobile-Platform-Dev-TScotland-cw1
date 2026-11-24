package com.example.s2366848;

import java.util.ArrayList;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.s2366848.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Receive traffic information from MainActivity
        ArrayList<String> titles =
                getIntent().getStringArrayListExtra("markerTitles");

        ArrayList<String> latitudes =
                getIntent().getStringArrayListExtra("markerLatitudes");

        ArrayList<String> longitudes =
                getIntent().getStringArrayListExtra("markerLongitudes");

        // Add a marker for every valid traffic situation
        if (titles != null && latitudes != null && longitudes != null) {

            for (int i = 0; i < titles.size(); i++) {
                try {
                    double latitude = Double.parseDouble(latitudes.get(i));
                    double longitude = Double.parseDouble(longitudes.get(i));

                    LatLng location = new LatLng(latitude, longitude);

                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(titles.get(i)));

                    // Centre the map on the first traffic situation
                    if (i == 0) {
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(location, 10)
                        );
                    }

                } catch (Exception e) {
                    // Skip invalid marker information
                }
            }
        }

        // Show Scotland if there are no traffic situations
        if (titles == null || titles.isEmpty()) {
            LatLng scotland = new LatLng(56.5, -4.2);
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(scotland, 6)
            );
        }
    }


}