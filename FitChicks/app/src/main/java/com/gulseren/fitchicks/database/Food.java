package com.gulseren.fitchicks.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "foods")
public class Food {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "userId")
    public int userId; // artık foreign key değil – normal integer

    @ColumnInfo(name = "foodName")
    public String foodName;

    @ColumnInfo(name = "calorie")
    public double calorie;

    @ColumnInfo(name = "mealType")
    public String mealType;

    @ColumnInfo(name= "date")
    public String date;

    public Food(int userId, String foodName, double calorie, String mealType, String date) {
        this.userId = userId;
        this.foodName = foodName;
        this.calorie = calorie;
        this.mealType = mealType;
        this.date = date;
    }

    //Getters
    public int getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public double getCalorie() {
        return calorie;
    }
    public String getMealType() {
        return mealType;
    }
    public int getUserId() {
        return userId;
    }
    public String getDate() {return date;}
}