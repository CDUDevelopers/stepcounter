package com.example.stepcounterapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


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
        //todo fix input exceptions
        //todo make inputs auto clear when selected
        EditText nameInput = findViewById(R.id.nameChangeTextbox);
        String name = nameInput.getText().toString();
        EditText genderInput = findViewById(R.id.genderChangeTextbox);
        String gender = genderInput.getText().toString();
        EditText ageInput = findViewById(R.id.ageChangeTextbox);
        int age = Integer.parseInt(ageInput.getText().toString());
        EditText weightInput = findViewById(R.id.weightChangeTextbox);
        Float weight = Float.valueOf(weightInput.getText().toString());
        EditText heightInput = findViewById(R.id.heightChangeTextbox);
        int height = Integer.parseInt(heightInput.getText().toString());

        UserDatabase db = new UserDatabase(this);
        db.open();

        if (db.changeUsername(user.getUsername(), name, user)) {
            user.updateUsername(name);
        }
        if (!gender.equals(user.getGender())) {
            user.updateGender(gender);
        }
        if (age != user.getAge()) {
            user.updateAge(age);
        }
        if (weight != user.getWeight()) {
            user.updateWeight(weight);
        }
        if (height != user.getHeight()) {
            user.updateHeight(height);
        }
        db.close();
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
    }
}
