package com.gulseren.fitchicks.date;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.gulseren.fitchicks.database.AppDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DailyResetWorker extends Worker {

    private AppDatabase database;

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        database = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) return Result.failure();

        // Gün değişimini kontrol et
        String lastDate = sharedPreferences.getString("lastOpenedDate", "");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());

        if (!today.equals(lastDate)) {
            sharedPreferences.edit().putString("lastOpenedDate", today).apply();
        }

        return Result.success();
    }
}
