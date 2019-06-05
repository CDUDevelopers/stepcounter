package com.example.stepcounterapp;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.Serializable;
import java.text.DecimalFormat;

public class User implements Serializable {
    private int steps;
    private int calories;
    private int distance;
    private float weight;
    private int height;
    private int age;
    private long exerciseTime;
    private String username;
    private String gender;

    //todo add linked bt device auto connect so can resume without reconnecting

    public User() {

    }

    public User(int steps, int calories, int distance, float weight, int height, int age, long exerciseTime, String username, String gender) {
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.exerciseTime = exerciseTime;
        this.username = username;
        this.gender = gender;
    }
    public void updateSteps(int steps) {
        this.steps = steps;
    }
    public int getSteps() {
        return steps;
    }

    public void updateCalories(int calories) {
        this.calories = calories;
    }
    public int getCalories() {
        return calories;
    }

    public void updateWeight(float weight) {
        this.weight = weight;
    }
    public float getWeight() {
        return weight;
    }

    public void updateHeight(int height) {
        this.height = height;
    }
    public int getHeight() {
        return height;
    }

    public void updateAge(int age) {
        this.age = age;
    }
    public int getAge() {
        return age;
    }

    public void updateExerciseTime(long exerciseTime) {
        this.exerciseTime = exerciseTime;
    }
    public long getExerciseTime() {
        return exerciseTime;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getMinutes(long milliSeconds) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);

        int value;
        long temp = milliSeconds / 1000 / 60;

        value = Math.toIntExact(temp);

        return value;
    }

    public void updateUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void updateDistance(int distance) {
        this.distance = distance;
    }
    public int getDistance() {
        return distance;
    }
    public double getKM(int meters) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);

        double value;
        double temp = meters / 1000.0;

        value = Double.parseDouble(format.format(temp));

        return value;
    }

    public void updateGender(String gender) {
        this.gender = gender;
    }
    public String getGender() {
        return gender;
    }

    //----------------------------------------------------------------------------------------------

    public void testData(Context context) {
        UserDatabase db = new UserDatabase(context);
        db.open();

        db.historicTableCheat(new User(steps, calories, distance, weight, height, age, exerciseTime, username, gender), 7);

        db.close();
    }
}
