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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Date;

public class Walking extends AppCompatActivity {
    private User user;
    private BTService btService;

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;
    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarData BARDATA ;

    private int startSteps;
    private int startCalories;
    private int startDistance;
    private long startTime;
    private Boolean isExercising;
    TextView stepCount;
    TextView calorieCount;
    TextView distanceCount;

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
        setContentView(R.layout.activity_walking);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        chart = (BarChart) findViewById(R.id.chart1);

        BARENTRY = new ArrayList<>();

        BarEntryLabels = new ArrayList<String>();

        AddValuesToBARENTRY();

        AddValuesToBarEntryLabels();

        Bardataset = new BarDataSet(BARENTRY, "Projects");

        BARDATA = new BarData(BarEntryLabels, Bardataset);

        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        chart.setData(BARDATA);

        chart.animateY(3000);

        //------------------------------------------------------------------------------------------

        isExercising = false;
        stepCount = findViewById(R.id.stepCountWalking);
        calorieCount = findViewById(R.id.calorieCountWalking);
        distanceCount = findViewById(R.id.distanceCountWalking);

        final Button toggleExerciseButton = findViewById(R.id.toggleWalkingButton);
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

    //runs when the page is brought to the foreground of the device screen
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

    public void AddValuesToBARENTRY(){

        BARENTRY.add(new BarEntry(2f, 0));
        BARENTRY.add(new BarEntry(4f, 1));
        BARENTRY.add(new BarEntry(6f, 2));
        BARENTRY.add(new BarEntry(8f, 3));
        BARENTRY.add(new BarEntry(7f, 4));
        BARENTRY.add(new BarEntry(3f, 5));

    }
    public void AddValuesToBarEntryLabels(){

        BarEntryLabels.add("January");
        BarEntryLabels.add("February");
        BarEntryLabels.add("March");
        BarEntryLabels.add("April");
        BarEntryLabels.add("May");
        BarEntryLabels.add("June");

    }
    public void checkin(View v) {
        Intent i = new Intent(Walking.this,MapsActivity2.class);
        startActivity(i);

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
        db.updateExerciseDB(user.getUsername(), endSteps, endCalories, endDistance, endTime);

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
            //todo on successful connect go home
        }
    };

    private static IntentFilter generateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTService.stepCountUpdateString);

        return intentFilter;
    }
}
