package com.gulseren.fitchicks.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class KullaniciBilgi extends AppCompatActivity {
    TextView isim, yas, boy, kilo, mail;

    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kullanicibilgi);

        isim = findViewById(R.id.isim);
        yas = findViewById(R.id.yas);
        boy = findViewById(R.id.boy);
        kilo = findViewById(R.id.kilo);
        mail = findViewById(R.id.mail);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        database = AppDatabase.getInstance(this);
        database.userDao().getById(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                            isim.setText(user.getUsername());
                            yas.setText(String.valueOf(user.getAge()));
                            boy.setText(String.valueOf(user.getHeight()));
                            kilo.setText(String.valueOf(user.getWeight()));
                            mail.setText(user.getEmail());
                        }
                        , throwable -> {
                            Toast.makeText(this, "Kullanıcı bilgisi alınamadı!", Toast.LENGTH_SHORT).show();
                        });


    }
}
