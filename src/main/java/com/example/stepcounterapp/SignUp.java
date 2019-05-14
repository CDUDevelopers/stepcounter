package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void submitNewUser(View view) {
        //Todo have the new user saved to database and put into storage
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }
}
