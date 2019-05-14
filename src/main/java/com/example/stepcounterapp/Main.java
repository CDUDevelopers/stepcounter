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
import android.widget.TextView;

public class homeScreen extends AppCompatActivity {
    private User user;
    private BTService btService;

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
        setContentView(R.layout.activity_home_screen);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //update the numbers on screen to the users stored values
        updateWeightIndication();
        updateStepDisplay();
        updateCalorieDisplay();
        updateDistanceDisplay();
    }
    //todo update onResume and onPause methods to account for broadcast reciver
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

    //todo add onStop() or onDestroy() override
    //todo include database update in them to save data

    public void stepInfoPage(View view) {
        Intent intent = new Intent(this, stepCountScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void bluetoothPage(View view) {
        Intent intent = new Intent(this, BluetoothConnectionScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void caloireInfoPage(View view) {
        Intent intent = new Intent(this, calorieScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void weightInfoScreen(View view) {
        //todo add a button to add a new weight measurement
        Intent intent = new Intent(this, weightScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void exerciseInfoPage(View view) {
        Intent intent = new Intent(this, exerciseScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
    //------------------------------------------------------------------------------------

    private void updateWeightIndication() {
        TextView weightView = findViewById(R.id.currentWeightDisplay);
        //Todo Check Null pointer exceptions handling
        weightView.setText(user.getWeight() + "kg");
    }
    public void recordNewWeightPage(View view) {
        //Todo Make weight entry screen and functions
        Intent intent = new Intent(this, stepCountScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
    //Todo auto updating step counter(add monthly)
    private void updateStepDisplay() {
        TextView dailyStepView = findViewById(R.id.dailyStepCountTextview);
        dailyStepView.setText("Steps Today:\n" + user.getSteps());
    }
    private void updateCalorieDisplay() {
        TextView dailyCalorieView = findViewById(R.id.dailyCaloriesTextview);
        dailyCalorieView.setText("Calories Today:\n" + user.getCalories());
    }
    private void updateDistanceDisplay() {
        TextView dailyDistanceView = findViewById(R.id.dailyDistanceTextview);
        dailyDistanceView.setText("Distance Today(m):\n" + user.getDistance());
    }


    private final BroadcastReceiver updateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BTService.stepCountUpdateString)) {
                user.updateSteps(intent.getIntExtra("Steps", 0));
                user.updateCalories(intent.getIntExtra("Calories", 0));
                user.updateDistance(intent.getIntExtra("Distance", 0));
                //todo use step data
                updateStepDisplay();
                updateCalorieDisplay();
                updateDistanceDisplay();
            }else {
                System.out.println("broadcast receiver error");
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