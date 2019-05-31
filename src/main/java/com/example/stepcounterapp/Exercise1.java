package com.example.stepcounterapp;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

public class Exercise1 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private User user;
    private BTService btService;

    private int startSteps;
    private int startCalories;
    private int startDistance;
    private long startTime;
    private Boolean isExercising;
    private ArrayList<Double> lat;
    private ArrayList<Double> lng;

    private TextView pageTitle;
    private TextView stepCount;
    private TextView calorieCount;
    private TextView distanceCount;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationMarker;
    public static final int REQEST_LOCATION_CODE = 99;
    private static final String TAG = "Exercise Map";



    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BTService.LocalBinder) service).getService();
            if (btService.needStart()) {
                btService.start((BluetoothManager) getSystemService(BLUETOOTH_SERVICE));
            }

            //attempt auto-connect
            if (btService.getDeviceAddress() != null) {
                btService.gattConnect(btService.getDeviceAddress());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise1);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        pageTitle = findViewById(R.id.exercisePageTitle);
        pageTitle.setText((String)i.getSerializableExtra("exerciseType"));

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //------------------------------------------------------------------------------------------

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //------------------------------------------------------------------------------------------

        isExercising = false;
        stepCount = findViewById(R.id.exerciseStepCountTextview);
        calorieCount = findViewById(R.id.exerciseCalorieCountTextview);
        distanceCount = findViewById(R.id.exerciseDistanceCountTextview);

        final Button toggleExerciseButton = findViewById(R.id.toggleExerciseButton);
        toggleExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleExerciseButton.getText().toString().equals("Start")) {
                    toggleExerciseButton.setText("Stop");
                    isExercising = true;
                    startExercise();
                } else if (toggleExerciseButton.getText().toString().equals("Stop")) {
                    toggleExerciseButton.setText("Start");
                    isExercising = false;
                    stopExercise();
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(updateReciver, generateIntentFilter());
        if (btService != null) {
            btService.gattConnect(btService.getDeviceAddress());
        }
    }

    //runs when the page is removed from te foreground of the device screen
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReciver);
    }

    @Override
    protected void onStop() {
        UserDatabase db = new UserDatabase(this);
        db.open();
        db.saveUser(user);
        db.close();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ExerciseInformation.class);
        intent.putExtra("userData", user);
        intent.putExtra("exerciseType", pageTitle.getText().toString());
        startActivity(intent);
        finish();
    }

    private void startExercise() {
        startTime = new Date().getTime();
        startSteps = user.getSteps();
        startCalories = user.getCalories();
        startDistance = user.getDistance();

        lat = new ArrayList<Double>();
        lng = new ArrayList<Double>();

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

        stepCount.setText("Steps: " + (user.getSteps() - startSteps));
        calorieCount.setText("Calories: " + (user.getCalories() - startCalories) + " kcal");
        distanceCount.setText("Distance: " + user.getKM(user.getDistance() - startDistance) + " km");
    }

    private void stopExercise() {
        long endTime = new Date().getTime() - startTime;
        int endSteps = user.getSteps() - startSteps;
        int endCalories =  user.getCalories() - startCalories;
        int endDistance =  user.getDistance() - startDistance;

        UserDatabase db = new UserDatabase(this);
        db.open();

        db.updateExerciseDB(user.getUsername(), endSteps, endCalories, endDistance, endTime, pageTitle.getText().toString(), lat, lng);

        db.close();
    }

//--------------------------------------------------------------------------------------------------
    private final BroadcastReceiver updateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BTService.stepCountUpdateString)) {
                user.updateSteps(intent.getIntExtra("Steps", 0));
                user.updateCalories(intent.getIntExtra("Calories", 0));
                user.updateDistance(intent.getIntExtra("Distance", 0));

                if (isExercising) {
                    stepCount.setText("Steps: " + (user.getSteps() - startSteps));
                    calorieCount.setText("Calories: " + (user.getCalories() - startCalories) + " kcal");
                    distanceCount.setText("Distance: " + user.getKM(user.getDistance() - startDistance) + " km");
                }
            }else {
                System.out.println("broadcast receiver 'Unknown broadcast error'");
            }
        }
    };

    private static IntentFilter generateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTService.stepCountUpdateString);

        return intentFilter;
    }

//--------------------------------------------------------------------------------------------------
    //Map activity

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();
                }
                return;
        }
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
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

    }

    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastlocation != null) {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()))
                    .width(5)
                    .color(Color.BLUE));
        }
        lastlocation = location;

        //------------------------------------------------------------------------------------------

//todo find the bit that moves the camera and stop it from jerking the camera around at the least zoom it out a bit
        if (isExercising) {
            if (lat.size() != 0 && lng.size() != 0) {
                if (lat.get(lat.size() - 1) != location.getLatitude() || lng.get(lng.size() - 1) != location.getLongitude()) {
                    lat.add(location.getLatitude());
                    lng.add(location.getLongitude());
                }
            } else {
                lat.add(location.getLatitude());
                lng.add(location.getLongitude());
            }
        }
        //------------------------------------------------------------------------------------------

        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }
        //store location to string
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //get cordinate of the location
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(5));

        if(!isExercising)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(Exercise1.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

