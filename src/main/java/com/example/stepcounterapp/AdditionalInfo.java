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
            dayCount.setText("Daily:\n" + user.getCalories() + " kcal");
            weekCount.setText("Weekly:\n" + db.getWeeklyCalories(user) + " kcal");
            monthCount.setText("Monthly:\n" + db.getMonthlyCalories(user) + " kcal");
        } else if (pageTitle.getText().toString().equals("Distance")) {
            dayCount.setText("Daily:\n" + user.getKM(user.getDistance()) + " km");
            weekCount.setText("Weekly:\n" + user.getKM(db.getWeeklyDistance(user)) + " km");
            monthCount.setText("Monthly:\n" + user.getKM(db.getMonthlyDistance(user)) + " km");
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
            dayCount.setText("Daily:\n" + user.getCalories() + " kcal");
        } else if (pageTitle.getText().toString().equals("Distance")) {
            dayCount.setText("Daily:\n" + user.getKM(user.getDistance()) + " km");
        } else {
            System.out.println("Day value data type was an unexpected value");
        }
    }

    //----------------------------------------------------------------------------------------------

    //todo add data to these bar charts
    public void AddValuesToBARENTRY(){
        UserDatabase db = new UserDatabase(this);
        db.open();

       if (pageTitle.getText().toString().equals("Step")) {
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 0), 0));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 1), 1));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 2), 2));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 3), 3));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 4), 4));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 5), 5));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 6), 6));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 7), 7));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 8), 8));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 9), 9));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 10), 10));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 11), 11));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 12), 12));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 13), 13));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 14), 14));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 15), 15));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 16), 16));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 17), 17));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 18), 18));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 19), 19));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 20), 20));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 21), 21));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 22), 22));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 23), 23));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 24), 24));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 25), 25));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 26), 26));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 27), 27));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 28), 28));
           BARENTRY.add(new BarEntry(db.getDaysSteps(user, 29), 29));
       } else if (pageTitle.getText().toString().equals("Calorie")) {
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 0), 0));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 1), 1));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 2), 2));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 3), 3));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 4), 4));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 5), 5));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 6), 6));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 7), 7));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 8), 8));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 9), 9));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 10), 10));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 11), 11));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 12), 12));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 13), 13));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 14), 14));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 15), 15));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 16), 16));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 17), 17));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 18), 18));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 19), 19));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 20), 20));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 21), 21));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 22), 22));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 23), 23));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 24), 24));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 25), 25));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 26), 26));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 27), 27));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 28), 28));
           BARENTRY.add(new BarEntry(db.getDaysCalories(user, 29), 29));
       } else if (pageTitle.getText().toString().equals("Distance")) {
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 0)), 0));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 1)), 1));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 2)), 2));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 3)), 3));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 4)), 4));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 5)), 5));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 6)), 6));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 7)), 7));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 8)), 8));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 9)), 9));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 10)), 10));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 11)), 11));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 12)), 12));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 13)), 13));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 14)), 14));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 15)), 15));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 16)), 16));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 17)), 17));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 18)), 18));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 19)), 19));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 20)), 20));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 21)), 21));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 22)), 22));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 23)), 23));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 24)), 24));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 25)), 25));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 26)), 26));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 27)), 27));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 28)), 28));
           BARENTRY.add(new BarEntry((float) user.getKM(db.getDaysDistance(user, 29)), 29));
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
        BarEntryLabels.add("8 Days Ago");
        BarEntryLabels.add("9 Days Ago");
        BarEntryLabels.add("10 Days Ago");
        BarEntryLabels.add("11 Days Ago");
        BarEntryLabels.add("12 Days Ago");
        BarEntryLabels.add("13 Days Ago");
        BarEntryLabels.add("14 Days Ago");
        BarEntryLabels.add("15 Days Ago");
        BarEntryLabels.add("16 Days Ago");
        BarEntryLabels.add("17 Days Ago");
        BarEntryLabels.add("18 Days Ago");
        BarEntryLabels.add("19 Days Ago");
        BarEntryLabels.add("20 Days Ago");
        BarEntryLabels.add("21 Days Ago");
        BarEntryLabels.add("22 Days Ago");
        BarEntryLabels.add("23 Days Ago");
        BarEntryLabels.add("24 Days Ago");
        BarEntryLabels.add("25 Days Ago");
        BarEntryLabels.add("26 Days Ago");
        BarEntryLabels.add("27 Days Ago");
        BarEntryLabels.add("28 Days Ago");
        BarEntryLabels.add("29 Days Ago");
        BarEntryLabels.add("30 Days Ago");
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
