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

public class Main extends AppCompatActivity {
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
        setContentView(R.layout.activity_main);
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

    //----------------------------------------------------------------------------
    public void walking(View v) {
        Intent i = new Intent(Main.this, Walking.class);
        i.putExtra("userData", user);//todo other side
        startActivity(i);

    }
    public void hiking(View v) {
        Intent i = new Intent(Main.this, Hiking.class);
        i.putExtra("userData", user);//todo other side
        startActivity(i);

    }
    public void running(View v) {
        Intent i = new Intent(Main.this, Running.class);
        i.putExtra("userData", user);//todo other side
        startActivity(i);

    }
    public void edit(View v) {
        Intent i = new Intent(Main.this, Profile.class);
        i.putExtra("userData", user);//todo other side
        startActivity(i);

    }
    //-------------------------------------------------------------------------------

    public void bluetoothPage(View view) {//todo add and link the button for this
        Intent intent = new Intent(this, BluetoothConnectionSetup.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
    //------------------------------------------------------------------------------------

    private void updateWeightIndication() {
        TextView weightView = findViewById(R.id.weightDisplay);
        //Todo Check Null pointer exceptions handling
        weightView.setText(user.getWeight() + "kg");
    }
    //Todo auto updating step counter(add monthly)
    private void updateStepDisplay() {
       TextView dailyStepView = findViewById(R.id.daliyStepsTextbox);
       dailyStepView.setText("Steps Today:\n" + user.getSteps());
    }
    private void updateCalorieDisplay() {
        TextView dailyCalorieView = findViewById(R.id.daliyCaloriesTextbox);
        dailyCalorieView.setText("Calories Today:\n" + user.getCalories());
    }
    private void updateDistanceDisplay() {
        TextView dailyDistanceView = findViewById(R.id.daliyDistanceTextbox);
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