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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class AdditionalInfo extends AppCompatActivity {
    private User user;
    private BTService btService;
    private TextView pageTitle;

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;
    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarData BARDATA ;


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
        setContentView(R.layout.activity_additional_info);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        pageTitle = findViewById(R.id.moreInfoPageTitle);
        pageTitle.setText((String)i.getSerializableExtra("dataType"));

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //------------------------------------------------------------------------------------------

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

        setPageConent();
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
    public void onBackPressed() {
        UserDatabase db = new UserDatabase(this);
        db.open();
        db.saveUser(user);
        db.close();
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------

    private void setPageConent() {
        TextView dayCount, weekCount, monthCount;
        dayCount = findViewById(R.id.moreInfoDayTextview);
        weekCount = findViewById(R.id.moreInfoWeekTextview);
        monthCount = findViewById(R.id.moreInfoMonthTextview);

        UserDatabase db = new UserDatabase(this);
        db.open();
        //todo check if the hisorical numbers include today or not(dont think they do)

        if (pageTitle.getText().toString().equals("Step")) {
            dayCount.setText("Daily:\n" + user.getSteps());
            weekCount.setText("Weekly:\n" + db.getWeeklySteps(user));
            monthCount.setText("Monthly:\n" + db.getMonthlySteps(user));
        } else if (pageTitle.getText().toString().equals("Calorie")) {
            dayCount.setText("Daily:\n" + user.getCalories());
            weekCount.setText("Weekly:\n" + db.getWeeklyCalories(user));
            monthCount.setText("Monthly:\n" + db.getMonthlyCalories(user));
        } else if (pageTitle.getText().toString().equals("Distance")) {
            dayCount.setText("Daily:\n" + user.getDistance());
            weekCount.setText("Weekly:\n" + db.getWeeklyDistance(user));
            monthCount.setText("Monthly:\n" + db.getMonthlyDistance(user));
        } else {
            System.out.println("Page header pass was an unexpected value");
        }
        db.close();

        //todo set bar chart content
    }

    private void updateDayValues() {
        TextView dayCount = findViewById(R.id.moreInfoDayTextview);

        if (pageTitle.getText().toString().equals("Step")) {
            dayCount.setText("Daily:\n" + user.getSteps());
        } else if (pageTitle.getText().toString().equals("Calorie")) {
            dayCount.setText("Daily:\n" + user.getCalories());
        } else if (pageTitle.getText().toString().equals("Distance")) {
            dayCount.setText("Daily:\n" + user.getDistance());
        } else {
            System.out.println("Day value data type was an unexpected value");
        }
    }

    //----------------------------------------------------------------------------------------------

    //todo add data to these bar charts
    public void AddValuesToBARENTRY(){
        UserDatabase db = new UserDatabase(this);
        db.open();

       /* db.stepCheat(100, 43, 5, 0);
        db.stepCheat(456, 1, 63, 1);
        db.stepCheat(243, 543, 916, 2);
        db.stepCheat(8, 871, 231, 3);
        db.stepCheat(45, 4397, 1735, 4);
        db.stepCheat(1209, 215, 23813, 5);
        db.stepCheat(24876, 21, 0, 6);**/

       if (pageTitle.getText().toString().equals("Step")) {
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 0), 0));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 1), 1));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 2), 2));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 3), 3));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 4), 4));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 5), 5));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 6), 6));
       } else if (pageTitle.getText().toString().equals("Calorie")) {
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 0), 0));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 1), 1));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 2), 2));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 3), 3));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 4), 4));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 5), 5));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 6), 6));
       } else if (pageTitle.getText().toString().equals("Distance")) {
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 0), 0));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 1), 1));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 2), 2));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 3), 3));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 4), 4));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 5), 5));
           BARENTRY.add(new BarEntry(db.getDaysDistance(user, 6), 6));
       }


        db.close();
    }

    public void AddValuesToBarEntryLabels(){

        BarEntryLabels.add("Today");
        BarEntryLabels.add("Yesterday");
        BarEntryLabels.add("3 Days Ago");
        BarEntryLabels.add("4 Days Ago");
        BarEntryLabels.add("5 Days Ago");
        BarEntryLabels.add("6 Days Ago");
        BarEntryLabels.add("7 Days Ago");

    }

    //----------------------------------------------------------------------------------------------

    private final BroadcastReceiver updateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BTService.stepCountUpdateString)) {
                user.updateSteps(intent.getIntExtra("Steps", 0));
                user.updateCalories(intent.getIntExtra("Calories", 0));
                user.updateDistance(intent.getIntExtra("Distance", 0));
                //todo use step data

                updateDayValues();
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
