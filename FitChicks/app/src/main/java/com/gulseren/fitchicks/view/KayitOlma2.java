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
import com.gulseren.fitchicks.database.User;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class KayitOlma2 extends AppCompatActivity {
    EditText editYas, editBoy, editKilo, editCinsiyet;
    Button btnKaydol;
    AppDatabase userDatabase;
    String isim, email, sifre, cinsiyet;
    int yas, boy, kilo;
    SharedPreferences sharedPreferences;
    boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kayitolma2);

        editYas = findViewById(R.id.yasText);
        editBoy = findViewById(R.id.boyText);
        editKilo = findViewById(R.id.kiloText);
        editCinsiyet = findViewById(R.id.cinsiyetText);
        btnKaydol = findViewById(R.id.buttonn);

        //database
        userDatabase = AppDatabase.getInstance(this);

        btnKaydol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editYas.getText().toString().isEmpty() || editBoy.getText().toString().isEmpty() || editCinsiyet.getText().toString().isEmpty() || editKilo.getText().toString().isEmpty()) {
                    Toast.makeText(KayitOlma2.this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Kullanıcı bilgileri
                getIntentInfo();
                yas = Integer.parseInt(editYas.getText().toString());
                boy = Integer.parseInt(editBoy.getText().toString());
                kilo = Integer.parseInt(editKilo.getText().toString());
                cinsiyet = editCinsiyet.getText().toString();

                //Kullanıcıyı database'e kaydediyoruz
                User user = new User(isim, email, sifre, yas, cinsiyet, boy, kilo);
                userDatabase.userDao().insert(user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(id -> {
                            isLogged = true;
                            // id burada Long tipinde geliyor
                            sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("userId", id.intValue()); // Long -> int
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            Toast.makeText(KayitOlma2.this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
                            goHome();
                        }, throwable -> {
                            throwable.printStackTrace();
                        });

                        // DATABASE KONTROLÜ İÇİN //
                        /*.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            userDataBase.userDao().getAll()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(users -> {
                                        for (User u : users) {
                                            System.out.println(
                                                    "ID: " + u.getId() +
                                                            " USERNAME: " + u.getUsername() +
                                                            " EMAIL: " + u.getEmail() +
                                                            " PASSWORD: " + u.getPassword() +
                                                            " AGE: " + u.getAge() +
                                                            " GENDER: " + u.getGender() +
                                                            " HEIGHT: " + u.getHeight() +
                                                            " WEIGHT: " + u.getWeight()
                                            );
                                        }
                                    }, throwable -> throwable.printStackTrace());
                        }, throwable -> throwable.printStackTrace());

                Toast.makeText(KayitOlma2.this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
                goHome();
            }
        });*/
            }
        });
    }

    private void goHome() {
        Intent intent = new Intent(KayitOlma2.this, Anasayfa.class);
        startActivity(intent);
        finish();
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        isim = intent.getStringExtra("isim");
        email = intent.getStringExtra("email");
        sifre = intent.getStringExtra("sifre");
    }
}



