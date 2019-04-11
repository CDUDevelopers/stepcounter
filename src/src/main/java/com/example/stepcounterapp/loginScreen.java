package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class loginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
    }
// Change back to private after login fixed
    public void homePage(View view) {
        User user = new User();
        //Todo add SQL login and pull data from memory then add to the user object to pass
        //Todo add check for session in each onCreate call i.e. if (user.username = null) {go to login};

        Intent intent = new Intent(this, homeScreen.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
//Todo method for validating the login (not tested yet)
    public void submitLogin(View view) {
        EditText usernameEntry = findViewById(R.id.usernameEntry);
        String username = usernameEntry.getText().toString();
        EditText passwordEntry = findViewById(R.id.passwordEntry);
        String password = passwordEntry.getText().toString();

        userDatabase db = new userDatabase(loginScreen.this);
        db.open();

        if (db.Login(username, password)) {
            homePage(view);
        } else {
            //provide user feedback
        }
        db.close();
    }

    public void createAccountPage(View view) {
        Intent intent = new Intent(this, createUserLogin.class);

        startActivity(intent);
    }

}
