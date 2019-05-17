package com.example.stepcounterapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class Profile extends AppCompatActivity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");
    }

    public void submitProfileUpdate(View view) {
        EditText nameInput = findViewById(R.id.nameChangeTextbox);
        EditText genderInput = findViewById(R.id.genderChangeTextbox);

        EditText ageInput = findViewById(R.id.ageChangeTextbox);
        EditText weightInput = findViewById(R.id.weightChangeTextbox);
        EditText heightInput = findViewById(R.id.heightChangeTextbox);

        String name = nameInput.getText().toString();
        String gender = genderInput.getText().toString();

        UserDatabase db = new UserDatabase(this);
        db.open();

        if (!name.equals("")) {
            if (!name.equals(user.getUsername())) {
                if (db.changeUsername(user.getUsername(), name, user)) {
                    user.updateUsername(name);
                } else {
                    Toast.makeText(this, "Username Already taken", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (!gender.equals("")) {
            if (!gender.equals(user.getGender())) {
                user.updateGender(gender);
            }
        }
        if (!ageInput.getText().toString().equals("")) {
            int age = Integer.parseInt(ageInput.getText().toString());
            if (age != user.getAge()) {
                user.updateAge(age);
            }
        }
        if (!weightInput.getText().toString().equals("")) {
            Float weight = Float.valueOf(weightInput.getText().toString());
            if (weight != user.getWeight()) {
                user.updateWeight(weight);
            }
        }
        if (!heightInput.getText().toString().equals("")) {
            int height = Integer.parseInt(heightInput.getText().toString());
            if (height != user.getHeight()) {
                user.updateHeight(height);
            }
        }
        db.saveUser(user);
        db.close();
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }


}
