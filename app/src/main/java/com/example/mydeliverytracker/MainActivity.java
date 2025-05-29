package com.example.mydeliverytracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mydeliverytracker.databinding.ActivityMainBinding;
import com.example.mydeliverytracker.model.MainViewModel;
import com.example.mydeliverytracker.model.Route;
import com.example.mydeliverytracker.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.api.Status; // Updated import
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private MainViewModel viewModel;
    private ActivityMainBinding binding;
    private LatLng pickupLocation, dropOffLocation;
    private Polyline userPath;
    private List<LatLng> userPathPoints = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private BroadcastReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Places API
        Places.initialize(getApplicationContext(), "YOUR_API_KEY"); // Replace with your API key
        PlacesClient placesClient = Places.createClient(this);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupPlacesAutocomplete();
        setupMap();
        setupObservers();
        setupClickListeners();
        setupLocationReceiver();
        requestLocationPermissions();
    }

    private void setupPlacesAutocomplete() {
        // Initialize Pickup Autocomplete Fragment
        AutocompleteSupportFragment pickupFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.pickup_autocomplete_fragment);
        if (pickupFragment != null) {
            pickupFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            pickupFragment.setHint("Search Pickup Location");
            pickupFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    pickupLocation = place.getLatLng();
                    if (pickupLocation != null) {
                        googleMap.clear(); // Clear previous markers
                        googleMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup: " + place.getName()));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));
                        if (dropOffLocation != null) {
                            viewModel.fetchRoute(pickupLocation, dropOffLocation);
                        }
                    }
                }

                @Override
                public void onError(Status status) {
                    Log.e("MainActivity", "Error selecting pickup: " + status.getStatusMessage());
                    Toast.makeText(MainActivity.this, "Error selecting pickup: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize Drop-off Autocomplete Fragment
        AutocompleteSupportFragment dropoffFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.dropoff_autocomplete_fragment);
        if (dropoffFragment != null) {
            dropoffFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            dropoffFragment.setHint("Search Drop-off Location");
            pickupFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    dropOffLocation = place.getLatLng();
                    if (dropOffLocation != null) {
                        googleMap.addMarker(new MarkerOptions().position(dropOffLocation).title("Drop-off: " + place.getName()));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dropOffLocation, 15));
                        if (pickupLocation != null) {
                            viewModel.fetchRoute(pickupLocation, dropOffLocation);
                        }
                    }
                }

                @Override
                public void onError(Status status) {
                    Log.e("MainActivity", "Error selecting drop-off: " + status.getStatusMessage());
                    Toast.makeText(MainActivity.this, "Error selecting drop-off: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnMapClickListener(latLng -> {
            if (pickupLocation == null) {
                pickupLocation = latLng;
                googleMap.clear(); // Clear previous markers
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Pickup"));
            } else if (dropOffLocation == null) {
                dropOffLocation = latLng;
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Drop-off"));
                viewModel.fetchRoute(pickupLocation, dropOffLocation);
            }
        });
    }

    private void setupObservers() {
        viewModel.getRoute().observe(this, route -> {
            if (route != null) {
                drawRoute(route);
                binding.textEta.setText("ETA: " + route.getEta());
                binding.textDistance.setText("Distance: " + route.getDistance() + " km");
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonStartTracking.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startTrackingService();
            } else {
                Toast.makeText(this, "Location permission required to start tracking", Toast.LENGTH_SHORT).show();
                requestLocationPermissions();
            }
        });
    }

    private void setupLocationReceiver() {
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double lat = intent.getDoubleExtra("latitude", 0);
                double lng = intent.getDoubleExtra("longitude", 0);
                Log.d("MainActivity", "Received location: " + lat + ", " + lng);
                if (lat != 0 && lng != 0) {
                    LatLng newPosition = new LatLng(lat, lng);
                    updateUserPath(newPosition);
                    updateEtaAndDistance(newPosition);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver, new IntentFilter("LOCATION_UPDATE"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    private void drawRoute(Route route) {
        googleMap.clear(); // Clear previous markers and polylines
        if (pickupLocation != null) {
            googleMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup"));
        }
        if (dropOffLocation != null) {
            googleMap.addMarker(new MarkerOptions().position(dropOffLocation).title("Drop-off"));
        }
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(route.getPoints())
                .color(Color.BLUE)
                .width(10);
        googleMap.addPolyline(polylineOptions);
        // Zoom to fit the route
        if (!route.getPoints().isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : route.getPoints()) {
                builder.include(point);
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }
    }

    private void startTrackingService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);
        Toast.makeText(this, "Starting location tracking", Toast.LENGTH_SHORT).show();
    }

    private void updateUserPath(LatLng newPosition) {
        userPathPoints.add(newPosition);
        if (userPath != null) userPath.remove();
        userPath = googleMap.addPolyline(new PolylineOptions()
                .addAll(userPathPoints)
                .color(Color.RED)
                .width(8));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 15));
    }

    private void updateEtaAndDistance(LatLng currentPosition) {
        Route route = viewModel.getRoute().getValue();
        if (route == null) return;

        List<LatLng> remainingPoints = new ArrayList<>();
        double remainingDistance = 0;
        boolean found = false;

        for (LatLng point : route.getPoints()) {
            if (!found && isPointNear(currentPosition, point, 50)) {
                found = true;
            }
            if (found) {
                remainingPoints.add(point);
                if (remainingPoints.size() > 1) {
                    remainingDistance += SphericalUtil.computeDistanceBetween(
                            remainingPoints.get(remainingPoints.size() - 2),
                            remainingPoints.get(remainingPoints.size() - 1));
                }
            }
        }

        remainingDistance /= 1000.0; // Convert to km
        double averageSpeedKmh = 30; // Assume 30 km/h for ETA
        int etaSeconds = (int) (remainingDistance / averageSpeedKmh * 3600);
        String eta = String.format("%d min", etaSeconds / 60);

        binding.textEta.setText("ETA: " + eta);
        binding.textDistance.setText(String.format("Distance: %.2f km", remainingDistance));
    }

    private boolean isPointNear(LatLng point1, LatLng point2, double thresholdMeters) {
        return SphericalUtil.computeDistanceBetween(point1, point2) < thresholdMeters;
    }
}