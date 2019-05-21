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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ExerciseInformation extends AppCompatActivity {
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
        setContentView(R.layout.activity_exercise_information);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        pageTitle = findViewById(R.id.exerciseInfoPageTitle);
        pageTitle.setText((String)i.getSerializableExtra("exerciseType"));

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

        setPageContent();
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

    private void setPageContent() {
        TextView stepDay, stepWeek, stepMonth, calDay, calWeek, calMonth, distDay, distWeek, distMonth;
        stepDay = findViewById(R.id.stepCountDay);
        stepWeek = findViewById(R.id.stepCountWeek);
        stepMonth = findViewById(R.id.stepCountMonth);
        calDay = findViewById(R.id.calorieCountDay);
        calWeek = findViewById(R.id.calorieCountWeek);
        calMonth = findViewById(R.id.calorieCountMonth);
        distDay = findViewById(R.id.distanceCountDay);
        distWeek = findViewById(R.id.distanceCountWeek);
        distMonth = findViewById(R.id.distanceCountMonth);

        UserDatabase db = new UserDatabase(this);
        db.open();

        stepDay.setText("Steps Today:\n" + db.getDayExerciseSteps(user, pageTitle.getText().toString()));
        stepWeek.setText("Steps This Week:\n" + db.getWeekExerciseSteps(user, pageTitle.getText().toString()));
        stepMonth.setText("Steps This Month:\n" + db.getMonthExerciseSteps(user, pageTitle.getText().toString()));

        calDay.setText("Calories Today:\n" + db.getDayExerciseCalories(user, pageTitle.getText().toString()));
        calWeek.setText("Calories This Week:\n" + db.getWeekExerciseCalories(user, pageTitle.getText().toString()));
        calMonth.setText("Calories This Month:\n" + db.getMonthExerciseCalories(user, pageTitle.getText().toString()));

        distDay.setText("Distance Today:\n" + db.getDayExerciseDistance(user, pageTitle.getText().toString()));
        distWeek.setText("Distance This Week:\n" + db.getWeekExerciseDistance(user, pageTitle.getText().toString()));
        distMonth.setText("Distance This Month:\n" + db.getMonthExerciseDistance(user, pageTitle.getText().toString()));

        db.close();
    }

    private void goToExerciseRecordPage(View view) {
        Intent intent = new Intent(this, Exercise1.class);
        intent.putExtra("userData", user);
        intent.putExtra("exerciseType", pageTitle.getText().toString());
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------

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
