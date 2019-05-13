package com.example.stepcounterapp;

import java.io.Serializable;

public class User implements Serializable {
    private int steps;
    private int calories;
    private int distance;
    private float weight;
    private int height;
    private int age;
    private float exerciseTime;
    private String username;

    //todo add linked bt device auto connect so can resume without reconnecting

    public User() {

    }

    public User(int steps, int calories, int distance, float weight, int height, int age, float exerciseTime, String username) {
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.exerciseTime = exerciseTime;
        this.username = username;
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
    public void updateExerciseTime(float exerciseTime) {
        this.exerciseTime = exerciseTime;
    }
    public float getExerciseTime() {
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
}
