package com.gulseren.fitchicks.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "sports")
public class SportActivity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "userId")
    public int userId;
    @ColumnInfo(name = "sportActivity")
    String sportActivity;
    @ColumnInfo(name = "calorie")
    double calorie;
    @ColumnInfo(name = "time")
    String time;
    @ColumnInfo(name = "date")
    String date;


    public SportActivity(int userId, String sportActivity, double calorie, String time, String date) {
        this.userId = userId;
        this.sportActivity = sportActivity;
        this.calorie = calorie;
        this.time = time;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSportActivity() {
        return sportActivity;
    }

    public double getCalorie() {
        return calorie;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}
