package com.example.step_counter_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void walking(View v) {
        Intent i = new Intent(Main.this, Walking.class);
        startActivity(i);

    }
    public void hiking(View v) {
        Intent i = new Intent(Main.this, Hiking.class);
        startActivity(i);

    }
    public void running(View v) {
        Intent i = new Intent(Main.this, Running.class);
        startActivity(i);

    }
    public void edit(View v) {
        Intent i = new Intent(Main.this, Profile.class);
        startActivity(i);

    }

}
