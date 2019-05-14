package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class createUserLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_login);
    }

    public void submitNewUser(View view) {
        //Todo have the new user saved to database and put into storage
        Intent intent = new Intent(this, loginScreen.class);
        startActivity(intent);
    }
}
