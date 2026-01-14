package com.gulseren.fitchicks.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface FoodDao {
    @Insert
    Single<Long> insert(Food food);

    @Query("SELECT * FROM foods")
    Single<List<Food>> getAll();

    @Query("SELECT * FROM foods WHERE userId = :userId")
    Single<List<Food>> getByUserId(int userId);

    @Query("SELECT * FROM foods WHERE userId = :userId AND mealType = :mealType")
    Single<List<Food>> getByUserIdAndMealType(int userId, String mealType);

    @Query("SELECT SUM(calorie) FROM foods WHERE userId = :userId")
    Single<Integer> getTotalCaloriesForUser(int userId);

    @Query("SELECT * FROM foods WHERE userId = :userId")
    Single<List<Food>> getFoodsForUser(int userId);

    @Query("SELECT * FROM foods WHERE userId = :userId AND date = :date")
    Single<List<Food>> getByUserIdAndDate(int userId, String date);

    @Query("DELETE FROM foods WHERE userId = :userId AND date = :date")
    Completable deleteByUserIdAndDate(int userId, String date);
}