package com.gulseren.fitchicks.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;


@Dao
public interface SportDao {
    @Insert
    Single<Long> insert(SportActivity sportActivity);

    @Query("SELECT * FROM sports WHERE userId = :userId")
    Single<List<SportActivity>> getByUserId(int userId);
    @Query("DELETE FROM sports WHERE id = :id")
    void deleteActivity(int id);
    @Query("DELETE FROM sports WHERE userId = :userId")
    void deleteAllForUser(int userId);
    @Query("SELECT * FROM sports WHERE userId = :userId AND date = :date")
    Single<List<SportActivity>> getByUserIdAndDate(int userId, String date);
    @Query("DELETE FROM sports WHERE sportActivity = :activity AND date = :date AND userId = :userId")
    void deleteActivityForDay(int userId, String activity, String date);

}
