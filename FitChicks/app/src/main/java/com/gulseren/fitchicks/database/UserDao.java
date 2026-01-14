package com.gulseren.fitchicks.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) //Kullanıcı ekleme
    Single<Long> insert(User user);

    @Query("SELECT * FROM users")
    Single<List<User>> getAll();
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    Single<Integer> countByUsername(String username);
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    Single<Integer> countByEmail(String email);
    @Query("SELECT * FROM users WHERE (username = :input OR email = :input) AND password = :password LIMIT 1")
    Maybe<User> findByLogin(String input, String password);
    @Query("UPDATE users SET age = :age, gender = :gender, height = :height, weight = :weight WHERE id = :userId") //Kullanıcı bilgileri değişikliği
    Completable updateProfile(int userId, Integer age, String gender, Integer height, Integer weight);
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    Single<User> getById(int userId);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    Single<User> getByUsername(String username);

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    Single<User>getAnyUser();

    @Query("SELECT * FROM users WHERE (email = :input OR username = :input) AND password = :password LIMIT 1")
    Single<User> login(String input, String password);

}
