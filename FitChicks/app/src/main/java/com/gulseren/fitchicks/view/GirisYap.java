package com.gulseren.fitchicks.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GirisYap extends AppCompatActivity {
    EditText emailText, passwordText;
    Button giris, kayitolbuton;
    AppDatabase database;
    SharedPreferences sharedPreferences;
    boolean isLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("UserPrefs",MODE_PRIVATE);
        isLogged = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLogged) {
            // Kullanıcı zaten giriş yapmış ise direkt anasayfaya git
            Intent intent = new Intent(this, Anasayfa.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.girisyap);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        giris = findViewById(R.id.giris);
        kayitolbuton = findViewById(R.id.kayitolbuton);

        giris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(emailText, passwordText);
            }
        });
        kayitolbuton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GirisYap.this, KayitOlma1.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void login(EditText emailText, EditText passwordText) {
        String input = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        database = AppDatabase.getInstance(this);
        database.userDao().login(input, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {

                    SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("userId", user.getId());
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(GirisYap.this, "Giriş başarılı", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(GirisYap.this, Anasayfa.class);
                    startActivity(intent);
                    finish();

                }, throwable -> {
                    Toast.makeText(GirisYap.this, "Kullanıcı adı/email veya şifre yanlış", Toast.LENGTH_SHORT).show();
                });

    }
}
