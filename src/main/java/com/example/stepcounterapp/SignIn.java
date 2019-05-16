package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
    }
// Change back to private after login fixed
    public void homePage(View view, User user) {
        //Todo add SQL login and pull data from memory then add to the user object to pass
        //Todo add check for session in each onCreate call i.e. if (user.username = null) {go to login};
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
//Todo method for validating the login (not tested yet)
    public void submitLogin(View view) {//todo test login method
        Boolean loginSuccess = false;

        EditText usernameEntry = findViewById(R.id.usernameTextbox);
        String username = usernameEntry.getText().toString();
        EditText passwordEntry = findViewById(R.id.passwordTextbox);
        String password = passwordEntry.getText().toString();

        UserDatabase db = new UserDatabase(SignIn.this);
        db.open();

        loginSuccess = db.Login(username, password);

        if (loginSuccess) {
            User user;
            try {
                user = db.populateUserData(username);
                db.close();
                homePage(view, user);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            db.close();
            usernameEntry.setText("");
            passwordEntry.setText("");
            Toast.makeText(SignIn.this, "Username or password was incorrect. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void skipLogin(View view) {
        User user = new User();
        homePage(view, user);
    }

    public void createAccountPage(View view) {
        Intent intent = new Intent(this, SignUp.class);

        startActivity(intent);
    }

}
