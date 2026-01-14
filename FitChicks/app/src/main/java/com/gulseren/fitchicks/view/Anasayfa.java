package com.gulseren.fitchicks.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;
import com.gulseren.fitchicks.database.Food;
import com.gulseren.fitchicks.database.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Anasayfa extends AppCompatActivity {

    ImageButton menu, kaloriyuvarlagı;
    PopupWindow popupWindow;
    Button oguntakipgunluk, yapilacaklarBtn;
    TextView kalorihesaplama;
    AppDatabase database;
    SharedPreferences sp;
    String selected;
    double bmr;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.anasayfa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;

        });

        menu = findViewById(R.id.menu);
        oguntakipgunluk = findViewById(R.id.oguntakipgunluk);
        kaloriyuvarlagı = findViewById(R.id.kaloriyuvarlagı);
        kalorihesaplama = findViewById(R.id.kalorihesaplama);
        yapilacaklarBtn = findViewById(R.id.yapilacaklar);

        database = AppDatabase.getInstance(this);
        setListeners();

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sp.contains("bmr")) {
            bmr = Double.longBitsToDouble(sp.getLong("bmr", 0));
        } else {
            // DB'den çek
            int userId = sp.getInt("userId", -1);
            if (userId != -1) {
                database.userDao().getById(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                            double calculatedBMR = hesaplaBMR(user);
                            kalorihesaplama.setText(String.format(Locale.getDefault(), "0/%.0f kcal", calculatedBMR));

                            // SP'ye kaydet
                            sp.edit().putLong("bmr", Double.doubleToRawLongBits(calculatedBMR)).apply();
                        }, Throwable::printStackTrace);
            }
        }

        checkAndResetDailyData();


    }

    private void checkAndResetDailyData() {
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String lastDate = sp.getString("lastOpenedDate", "");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new java.util.Date());

        if (!today.equals(lastDate)) {
            sp.edit().putString("lastOpenedDate", today)
                    .putFloat("totalCalories", 0)
                    .apply();

            // Kullanıcıyı çek ve TextView'i güncelle
            int userId = sp.getInt("userId", -1);
            if (userId != -1) {
                database.userDao().getById(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                            double bmr = hesaplaBMR(user);
                            kalorihesaplama.setText(String.format(Locale.getDefault(), "0/%.0f kcal", bmr));
                        }, Throwable::printStackTrace);
            }
        }
    }

    private void setListeners() {
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        kaloriyuvarlagı.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Anasayfa.this, KaloriHesapla.class);
                startActivity(intent);
                finish();
            }
        });

        oguntakipgunluk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Anasayfa.this, OgunTakip.class);
                startActivity(intent);
                finish();
            }
        });

        yapilacaklarBtn.setOnClickListener(v -> {

            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int userId = sp.getInt("userId", -1);
            if (userId == -1) return;

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            database.sportDao().getByUserIdAndDate(userId, today)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(activities -> {

                        if (activities == null || activities.isEmpty()) {

                            Toast.makeText(
                                    Anasayfa.this,
                                    "Önce aktivite eklemelisiniz!",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            // Kayıt varsa direkt ToDoList sayfasına geç
                            Intent intent = new Intent(Anasayfa.this, ToDoList.class);
                            startActivity(intent);
                        }

                    }, Throwable::printStackTrace);
        });
    }

    private double hesaplaBMR(User user) {
        if (user.getGender().equalsIgnoreCase("kadın") || user.getGender().equalsIgnoreCase("k")) {
            bmr = 655.10 + (9.56 * user.getWeight()) + (1.85 * user.getHeight()) - (4.68 * user.getAge());
        } else {
            bmr = 66.47 + (13.75 * user.getWeight()) + (5 * user.getHeight()) - (6.76 * user.getAge());
        }
        return bmr;
    }

    private void showPopupMenu(View anchorView) {
        // Popup menüyü kapat (eğer açıksa)
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }

        // Layout inflater ile popup menüyü oluştur
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.menu, null);

        // PopupWindow oluştur
        popupWindow = new PopupWindow(popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Menü öğelerine tıklama olaylarını ekle
        TextView menuItem1 = popupView.findViewById(R.id.menuItem1);
        TextView menuItem2 = popupView.findViewById(R.id.menuItem2);
        TextView menuItem3 = popupView.findViewById(R.id.menuItem3);

        menuItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Anasayfa.this, OneriSayfam.class);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });

        menuItem2.setOnClickListener(v -> {
            Intent intent = new Intent(Anasayfa.this, KullaniciBilgi.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        menuItem3.setOnClickListener(v -> {
            showExitDialog();
            popupWindow.dismiss();
        });

        // Popup menüyü göster (sol üst köşede)
        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, 16, 50);
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Çıkış Yap");
        builder.setMessage("Emin misiniz?");
        builder.setPositiveButton("Evet", (dialog, which) -> {
            // Çıkış yap, isLoggedIn flag'ini false yap
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("currentUserId");
            editor.apply();

            Toast.makeText(Anasayfa.this, "Çıkış Yapıldı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Anasayfa.this, GirisYap.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Hayır", (dialog, which) -> {
            // Hiçbir şey yapma, anasayfada kal
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = sp.getString("lastOpenedDate", "");

        if (!todayDate.equals(lastDate)) {
            sp.edit()
                    .putFloat("totalCalories", 0)
                    .putString("lastOpenedDate", todayDate)
                    .apply();
        }

        int userId = sp.getInt("userId", -1);

        if (userId != -1) {
            // Kullanıcıyı databaseden çek → BMR'i HER SEFERİNDE güncelle
            database.userDao().getById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {

                        bmr = hesaplaBMR(user);  // ← ARTIK GÜNCEL BMR
                        sp.edit().putLong("bmr", Double.doubleToRawLongBits(bmr)).apply();

                        database.foodDao().getByUserIdAndDate(userId, todayDate)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(foods -> {
                                    double total = 0;
                                    for (Food food : foods) total += food.getCalorie();

                                    kalorihesaplama.setText(
                                            String.format(Locale.getDefault(), "%.0f/%.0f", total, bmr)
                                    );
                                }, Throwable::printStackTrace);

                    }, Throwable::printStackTrace);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String yeniAktivite = data.getStringExtra("yeniAktivite");

            if (yeniAktivite != null) {
                Toast.makeText(this, "Yeni aktivite eklendi: " + yeniAktivite, Toast.LENGTH_SHORT).show();
            }
        }
    }


}



