package com.example.stepcounterapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
public class Walking extends AppCompatActivity {
    private User user;

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;
    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarData BARDATA ;

    private int startSteps;
    private int startCalories;
    private int startDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

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

        final Button toggleExerciseButton = findViewById(R.id.toggleWalkingButton);
        toggleExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleExerciseButton.getText().toString().equals("Start")) {

                }
                toggleExerciseButton.setText("Pause");
                startSteps = user.getSteps();
                startCalories = user.getCalories();
                startDistance = user.getDistance();

            }
        });
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

    }
    private void stopExercise() {

    }
}
