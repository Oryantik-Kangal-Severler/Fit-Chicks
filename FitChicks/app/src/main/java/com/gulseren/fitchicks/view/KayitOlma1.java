package com.gulseren.fitchicks.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class KayitOlma1 extends AppCompatActivity {

    EditText editIsim, editEmail, editSifre;
    Button btnDevam;
    AppDatabase userDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kayitolma1);

        editIsim = findViewById(R.id.editIsim);
        editEmail = findViewById(R.id.editEmail);
        editSifre = findViewById(R.id.editSifre);
        btnDevam = findViewById(R.id.buttonDevam);

        //database
        userDatabase = AppDatabase.getInstance(this);

        // devam et'e basınca diğer sayfaya geç kankam
        btnDevam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String isim = editIsim.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String sifre = editSifre.getText().toString().trim();

                if (isim.isEmpty() || email.isEmpty() || sifre.isEmpty()) {
                    Toast.makeText(KayitOlma1.this, "Lütfen boş olan tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(KayitOlma1.this, "Geçersiz e-mail adresi", Toast.LENGTH_SHORT).show();
                }
                if (sifre.length() < 6) {
                    Toast.makeText(KayitOlma1.this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show();
                }else{
                    // Database'de kullanıcı adı kontrolü
                    userDatabase.userDao().countByUsername(isim)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    usernameCount -> {
                                        if (usernameCount > 0) {
                                            Toast.makeText(KayitOlma1.this, "Bu kullanıcı adı zaten alınmış, lütfen başka bir kullanıcı adı deneyin.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // E-mail  kontrolü
                                        userDatabase.userDao().countByEmail(email)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(
                                                        emailCount -> {
                                                            if (emailCount > 0) {
                                                                Toast.makeText(KayitOlma1.this, "Bu email zaten alınmış, lütfen başka bir mail adresi deneyin.", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }

                                                            Intent gecis = new Intent(KayitOlma1.this, KayitOlma2.class);
                                                            gecis.putExtra("isim", isim);
                                                            gecis.putExtra("email", email);
                                                            gecis.putExtra("sifre", sifre);
                                                            startActivity(gecis);
                                                            finish();
                                                        }, throwable -> {
                                                            throwable.printStackTrace();
                                                        }
                                                );
                                    }, throwable -> throwable.printStackTrace());

                }
            }
        });

    }
}
