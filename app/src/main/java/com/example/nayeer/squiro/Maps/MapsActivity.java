package com.example.nayeer.squiro.Maps;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.example.nayeer.squiro.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String pseudo;
    private Double longitude, latitude;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        score = intent.getExtras().getInt("score", 0);
        pseudo = intent.getExtras().getString("pseudo", "unknownPlayer");
        longitude = intent.getExtras().getDouble("longitude",0);
        latitude = intent.getExtras().getDouble("latitude", 0);
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

        LatLng pos = new LatLng(latitude, longitude);
        //Ajout du marker associé à aux informations données dans l'intent.
        mMap.addMarker(new MarkerOptions().position(pos).title("Score: "+score+" | Pseudo: "+pseudo).snippet("latitude: "+latitude+" | longitude: "+longitude)).showInfoWindow();
        pointToPosition(pos);
    }

    private void pointToPosition(LatLng position) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(14).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
