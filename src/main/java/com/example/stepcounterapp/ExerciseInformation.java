package com.example.stepcounterapp;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

    boolean firstTime;


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

        AddValuesToBarEntryLabels();

        AddValuesToBARENTRY();

        Bardataset = new BarDataSet(BARENTRY, "Projects");

        BARDATA = new BarData(BarEntryLabels, Bardataset);

        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        chart.setData(BARDATA);

        chart.animateY(3000);

        //------------------------------------------------------------------------------------------

        setPageContent();

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Intent intent = new Intent(ExerciseInformation.this, MonthlySummary.class);
                intent.putExtra("userData", user);
                intent.putExtra("exerciseType", pageTitle.getText().toString());
                intent.putExtra("selectedMonth", BarEntryLabels.get(e.getXIndex()));
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        //------------------------------------------------------------------------------------------

        setSpinnerContent();

        firstTime = true;

        Spinner spinner = findViewById(R.id.exerciseSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstTime) {
                    Toast.makeText(ExerciseInformation.this, "item selected", Toast.LENGTH_SHORT).show();
                    TextView step, cal, dist;
                    step = findViewById(R.id.daySummarySteps);
                    cal = findViewById(R.id.daySummaryCal);
                    dist = findViewById(R.id.daySummaryDistance);

                    if (!parent.getItemAtPosition(position).toString().equals("No Exercise Records Found")) {
                        UserDatabase db = new UserDatabase(ExerciseInformation.this);
                        db.open();

                        step.setText("Steps: " + db.getDaySteps(user,pageTitle.getText().toString(), parent.getItemAtPosition(position).toString()));
                        cal.setText("Calories: " + db.getDayCalories(user,pageTitle.getText().toString(), parent.getItemAtPosition(position).toString()));
                        dist.setText("Distance: " + db.getDayDistance(user,pageTitle.getText().toString(), parent.getItemAtPosition(position).toString()));

                        db.close();
                    }
                }
                firstTime = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        calDay.setText("Calories Today:\n" + db.getDayExerciseCalories(user, pageTitle.getText().toString()) + " kcal");
        calWeek.setText("Calories This Week:\n" + db.getWeekExerciseCalories(user, pageTitle.getText().toString()) + " kcal");
        calMonth.setText("Calories This Month:\n" + db.getMonthExerciseCalories(user, pageTitle.getText().toString()) + " kcal");

        distDay.setText("Distance Today:\n" + user.getKM(db.getDayExerciseDistance(user, pageTitle.getText().toString())) + " km");
        distWeek.setText("Distance This Week:\n" + user.getKM(db.getWeekExerciseDistance(user, pageTitle.getText().toString())) + " km");
        distMonth.setText("Distance This Month:\n" + user.getKM(db.getMonthExerciseDistance(user, pageTitle.getText().toString())) + " km");

        db.close();
    }

    public void goToExerciseRecordPage(View view) {
        Intent intent = new Intent(this, Exercise1.class);
        intent.putExtra("userData", user);
        intent.putExtra("exerciseType", pageTitle.getText().toString());
        startActivity(intent);
    }

    private void setSpinnerContent() {
        Spinner spinner = findViewById(R.id.exerciseSpinner);

        UserDatabase db = new UserDatabase(this);
        db.open();
        ArrayList list = db.getExerciseDateArray(user, pageTitle.getText().toString());
        db.close();

        if (list.get(0).equals("empty")) {
            list.set(0, "No Exercise Records Found");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------

    public void AddValuesToBARENTRY(){
        UserDatabase db = new UserDatabase(this);
        db.open();

        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(0), pageTitle.getText().toString())), 0));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(1), pageTitle.getText().toString())), 1));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(2), pageTitle.getText().toString())), 2));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(3), pageTitle.getText().toString())), 3));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(4), pageTitle.getText().toString())), 4));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(5), pageTitle.getText().toString())), 5));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(6), pageTitle.getText().toString())), 6));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(7), pageTitle.getText().toString())), 7));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(8), pageTitle.getText().toString())), 8));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(9), pageTitle.getText().toString())), 9));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(10), pageTitle.getText().toString())), 10));
        BARENTRY.add(new BarEntry(user.getMinutes(db.getMonthExerciseTime(user, BarEntryLabels.get(11), pageTitle.getText().toString())), 11));

        db.close();

    }
    public void AddValuesToBarEntryLabels(){

        BarEntryLabels.add("January");
        BarEntryLabels.add("February");
        BarEntryLabels.add("March");
        BarEntryLabels.add("April");
        BarEntryLabels.add("May");
        BarEntryLabels.add("June");
        BarEntryLabels.add("July");
        BarEntryLabels.add("August");
        BarEntryLabels.add("September");
        BarEntryLabels.add("October");
        BarEntryLabels.add("November");
        BarEntryLabels.add("December");


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
