package com.example.stepcounterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignUp extends AppCompatActivity {

    //todo the app banner cover the username entry textbox remove it from the page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
        finish();
    }

    public void submitNewUser(View view) {
        EditText usernameEntry = findViewById(R.id.newUserUesrnameTextbox);
        String username = usernameEntry.getText().toString();
        EditText passwordEntry = findViewById(R.id.newUserpasswordTextbox);
        String password = passwordEntry.getText().toString();
        EditText passwordConfermEntry = findViewById(R.id.newUserPasswordConfermTextbox);
        String passwordConferm = passwordConfermEntry.getText().toString();

        int userCreateReturn;
        UserDatabase db = new UserDatabase(this);
        db.open();

        if (password.equals(passwordConferm)) {
            userCreateReturn = db.AddUser(username, password);

            if (userCreateReturn == -1) {
                Toast.makeText(this, "Username already taken please try another", Toast.LENGTH_SHORT).show();
            } else if (userCreateReturn == 0) {
                Toast.makeText(this, "Account successful created", Toast.LENGTH_SHORT).show();
                db.close();
                Intent intent = new Intent(this, SignIn.class);
                startActivity(intent);
            } else {
                System.out.println("unknown user login table error");
            }
        } else {
            Toast.makeText(this, "Passwords do not match please try again", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
}
