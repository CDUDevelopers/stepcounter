package com.example.stepcounterapp;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MonthlySummary extends AppCompatActivity {
    private User user;
    private BTService btService;
    private String pageTitle;
    private String month;


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
        setContentView(R.layout.activity_monthly_summary);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");
        month = (String)i.getSerializableExtra("selectedMonth");
        pageTitle = (String)i.getSerializableExtra("exerciseType");

        setPageContent();
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
        intent.putExtra("exerciseType", pageTitle);
        startActivity(intent);
        finish();
    }

    private void setPageContent() {
        TextView step, cal, dist;

        step = findViewById(R.id.monthlySummaryStepMonth);
        cal = findViewById(R.id.monthlySummaryCalorieMonth);
        dist = findViewById(R.id.monthlySummaryDistanceMonth);

        UserDatabase db = new UserDatabase(this);
        db.open();

        step.setText("Month Steps:\n" + db.getMonthSteps(user, month));
        cal.setText("Month Calories:\n" + db.getMonthCalories(user, month));
        dist.setText("Month Distance:\n" + db.getMonthDistance(user, month));

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
