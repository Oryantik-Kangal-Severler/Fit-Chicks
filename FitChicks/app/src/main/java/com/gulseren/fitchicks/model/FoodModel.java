package com.gulseren.fitchicks.model;

import com.google.gson.annotations.SerializedName;

public class FoodModel {
    @SerializedName("name")
    private String name;

    @SerializedName("calories")
    private double calories;

    public FoodModel() {}

    public FoodModel(String name, double calories) {
        this.name = name;
        this.calories = calories;
    }

    public String getName() {
        return name;
    }
    public double getCalories() {
        return calories;
    }
}
