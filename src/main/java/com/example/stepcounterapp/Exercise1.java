package com.example.stepcounterapp;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class Exercise1 extends AppCompatActivity {
    private User user;
    private BTService btService;

    private int startSteps;
    private int startCalories;
    private int startDistance;
    private long startTime;
    private Boolean isExercising;

    private TextView pageTitle;
    private TextView stepCount;
    private TextView calorieCount;
    private TextView distanceCount;


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
                    //todo go back to where ever?
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

    private void startExercise() {
        startTime = new Date().getTime();
        startSteps = user.getSteps();
        startCalories = user.getCalories();
        startDistance = user.getDistance();

        //todo add any code to track gps path
    }
    private void stopExercise() {
        long endTime = new Date().getTime() - startTime;
        int endSteps = user.getSteps() - startSteps;
        int endCalories =  user.getCalories() - startCalories;
        int endDistance =  user.getDistance() - startDistance;

        //todo add any code to stop gps tracking

        UserDatabase db = new UserDatabase(this);
        db.open();

        //todo add the map path to the update
        db.updateExerciseDB(user.getUsername(), endSteps, endCalories, endDistance, endTime, pageTitle.getText().toString());

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
                //todo use step data

                if (isExercising) {
                    //todo check these output correctly
                    stepCount.setText(user.getSteps() - startSteps);
                    calorieCount.setText(user.getCalories() - startCalories);
                    distanceCount.setText(user.getDistance() - startDistance);
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
}
