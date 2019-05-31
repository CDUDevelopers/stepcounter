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
import android.widget.EditText;
import android.widget.Toast;


public class Profile extends AppCompatActivity {
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
        setContentView(R.layout.activity_profile);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");
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
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
        finish();
    }

    public void submitProfileUpdate(View view) {
        EditText nameInput = findViewById(R.id.nameChangeTextbox);
        EditText genderInput = findViewById(R.id.genderChangeTextbox);

        EditText ageInput = findViewById(R.id.ageChangeTextbox);
        EditText weightInput = findViewById(R.id.weightChangeTextbox);
        EditText heightInput = findViewById(R.id.heightChangeTextbox);

        String name = nameInput.getText().toString();
        String gender = genderInput.getText().toString();

        UserDatabase db = new UserDatabase(this);
        db.open();

        if (!name.equals("")) {
            if (!name.equals(user.getUsername())) {
                if (db.changeUsername(user.getUsername(), name, user)) {
                    user.updateUsername(name);
                } else {
                    Toast.makeText(this, "Username Already taken", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (!gender.equals("")) {
            if (!gender.equals(user.getGender())) {
                user.updateGender(gender);
            }
        }
        if (!ageInput.getText().toString().equals("")) {
            int age = Integer.parseInt(ageInput.getText().toString());
            if (age != user.getAge()) {
                user.updateAge(age);
            }
        }
        if (!weightInput.getText().toString().equals("")) {
            Float weight = Float.valueOf(weightInput.getText().toString());
            if (weight != user.getWeight()) {
                user.updateWeight(weight);
            }
        }
        if (!heightInput.getText().toString().equals("")) {
            int height = Integer.parseInt(heightInput.getText().toString());
            if (height != user.getHeight()) {
                user.updateHeight(height);
            }
        }

        db.saveUser(user);
        db.close();

        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
        finish();
    }

    public void addDataToTable(View view) {
        user.testData(this);
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
}
