package com.example.stepcounterapp;

import java.io.Serializable;

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
    //todo add age to user and to database

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

    //todo do we need to be able to return the exercise time as anything other than milliseconds?
    public void updateExerciseTime(long exerciseTime) {
        this.exerciseTime = exerciseTime;
    }
    public long getExerciseTime() {
        return exerciseTime;
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

    public void updateGender(String gender) {
        this.gender = gender;
    }
    public String getGender() {
        return gender;
    }

    //----------------------------------------------------------------------------------------------

}
