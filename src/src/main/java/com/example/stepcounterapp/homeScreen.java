package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class homeScreen extends AppCompatActivity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        updateWeightIndication();
    }

    public void stepInfoPage(View view) {
        Intent intent = new Intent(this, stepCountScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void bluetoothPage(View view) {
        Intent intent = new Intent(this, bluetoothScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void caloireInfoPage(View view) {
        Intent intent = new Intent(this, calorieScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }

    public void weightInfoScreen(View view) {
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
        //weightView.setText("");
        //Todo Check Null pointer exceptions handling
        weightView.setText(user.getWeight() + "kg");
    }
    public void recordNewWeightPage(View view) {
        //Todo Make weight entry screen and functions
        Intent intent = new Intent(this, stepCountScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
    //Todo auto updating step counter
    //todo auto updating calories (note will not work but can be made before step counter is written)

}
