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
import android.widget.Toast;

public class Main extends AppCompatActivity {
    private User user;
    private BTService btService;
    private boolean successfulConnection;


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
        if (i.hasExtra("SuccessfulConnection")) {
            successfulConnection = i.getBooleanExtra("SuccessfulConnection", false);
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.logo);

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //update the numbers on screen to the users stored values
        updateWeightIndication();
        updateHeightIndication();
        updateAgeIndication();
        updateStepDisplay();
        updateCalorieDisplay();
        updateDistanceDisplay();

        if (successfulConnection) {
            Button btButton = findViewById(R.id.bluetoothButton);
            btButton.setText("Bluetooth Connected");
            //todo what if the connection fails
        }
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
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
        finish();
    }

    //----------------------------------------------------------------------------
    public void walking(View v) {
        Intent i = new Intent(Main.this, ExerciseInformation.class);
        i.putExtra("userData", user);
        i.putExtra("exerciseType", "Walking");
        startActivity(i);

    }
    public void hiking(View v) {
        Intent i = new Intent(Main.this, ExerciseInformation.class);
        i.putExtra("userData", user);
        i.putExtra("exerciseType", "Hiking");
        startActivity(i);

    }
    public void running(View v) {
        Intent i = new Intent(Main.this, ExerciseInformation.class);
        i.putExtra("userData", user);
        i.putExtra("exerciseType", "Running");
        startActivity(i);

    }
    public void edit(View v) {
        Intent i = new Intent(Main.this, Profile.class);
        i.putExtra("userData", user);
        startActivity(i);

    }
    //-------------------------------------------------------------------------------

    //todo provide bluetooth connection feedback
    public void bluetoothPage(View view) {
        Intent intent = new Intent(this, BluetoothConnectionSetup.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void stepInfo(View view) {
        Intent intent = new Intent(this, AdditionalInfo.class);
        intent.putExtra("userData", user);
        intent.putExtra("dataType", "Step");
        startActivity(intent);
    }
    public void calInfo(View view) {
        Intent intent = new Intent(this, AdditionalInfo.class);
        intent.putExtra("userData", user);
        intent.putExtra("dataType", "Calorie");
        startActivity(intent);
    }
    public void distInfo(View view) {
        Intent intent = new Intent(this, AdditionalInfo.class);
        intent.putExtra("userData", user);
        intent.putExtra("dataType", "Distance");
        startActivity(intent);
    }
    //------------------------------------------------------------------------------------
    private void updateWeightIndication() {
        TextView weightView = findViewById(R.id.weightDisplay);
        if (user.getWeight() != -1) {
            weightView.setText("Weight: " + user.getWeight() + "kg");
        } else {
            weightView.setText("Weight not recorded yet.");
        }
    }
    private void updateHeightIndication() {
        TextView weightView = findViewById(R.id.heightDisplay);
        if (user.getWeight() != -1) {
            weightView.setText("Height: " + user.getHeight() + "cm");
        } else {
            weightView.setText("Height not recorded yet.");
        }
    }
    private void updateAgeIndication() {
        TextView weightView = findViewById(R.id.ageDisplay);
        if (user.getWeight() != -1) {
            weightView.setText("Age: " + user.getAge());
        } else {
            weightView.setText("Age not recorded yet.");
        }
    }
    //----------------------------------------------------------------

    private void updateStepDisplay() {
       TextView dailyStepView = findViewById(R.id.daliyStepsTextbox);
       dailyStepView.setText("Steps Today:\n" + user.getSteps());
    }
    private void updateCalorieDisplay() {
        TextView dailyCalorieView = findViewById(R.id.daliyCaloriesTextbox);
        dailyCalorieView.setText("Calories Today:\n" + user.getCalories() + "kcal");
    }
    private void updateDistanceDisplay() {
        TextView dailyDistanceView = findViewById(R.id.daliyDistanceTextbox);
        dailyDistanceView.setText("Distance Today:\n" + user.getKM(user.getDistance()) + "km");
    }


    private final BroadcastReceiver updateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BTService.stepCountUpdateString)) {
                user.updateSteps(intent.getIntExtra("Steps", 0));
                user.updateCalories(intent.getIntExtra("Calories", 0));
                user.updateDistance(intent.getIntExtra("Distance", 0));

                updateStepDisplay();
                updateCalorieDisplay();
                updateDistanceDisplay();
            } else if (action.equals("btDisconnected")) {
                Toast.makeText(Main.this, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
                Button btButton = findViewById(R.id.bluetoothButton);
                btButton.setText("Connect Bluetooth");
            } else {
                System.out.println("broadcast receiver error");
            }
        }
    };

    private static IntentFilter generateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTService.stepCountUpdateString);

        return intentFilter;
    }
}
