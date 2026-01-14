package com.gulseren.fitchicks.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;
import com.gulseren.fitchicks.database.Food;
import com.gulseren.fitchicks.database.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OgunTakip extends AppCompatActivity {

    ImageButton yuruyus, kosmak, bisikletsur, ipatlama, gerigit, ilerigit;
    String sportType;
    Button kahvalti, ogleYemegi, aksamYemegi, araOgun, homeBtn, addFoodBtn;
    TextView txtBreakfastTotal, txtLunchTotal, txtDinnerTotal, txtSnackTotal, totalCalorieTxt, fazlaKaloriTxt;
    LinearLayout layoutBreakfastConetnt, cardBreakfast, breakfastItems, layoutLunchContent, cardLunch, lunchItems, layoutDinnerContent, cardDinner, dinnerItems, layoutSnackContent, cardSnack, snackItems;
    List<Food> breakfastList = new ArrayList<>();
    List<Food> lunchList = new ArrayList<>();
    List<Food> dinnerList = new ArrayList<>();
    List<Food> snackList = new ArrayList<>();
    String mealType;
    String foodName;
    double foodCal;
    AppDatabase foodDatabase;
    SharedPreferences sharedPreferences;
    TextView dateTxt;
    double bmr;
    SimpleDateFormat sdf;
    String today;
    Date currentDate;
    int userId = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oguntakip);

        initalize();
        setListener();
        getIntentInfo();

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        sdf = new SimpleDateFormat("dd MMMM", new Locale("tr", "TR")); // ekranda görünecek olan tarih
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); //database için tarih
        currentDate = new Date(); // bugünün tarihi

        today = dbFormat.format(currentDate); //db ye kaydetmek için istenen format

        dateTxt.setText(sdf.format(currentDate)); // ekranda görünecek olan tarih için

        foodDatabase = AppDatabase.getInstance(this);

        // BMR'yi al ve totalCalorieTxt'i başlat
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId != -1) {
            foodDatabase.userDao().getById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                        bmr = hesaplaBMR(user);
                        totalCalorieTxt.setText(String.format(Locale.getDefault(), "0/%.0f kcal", bmr));
                    }, Throwable::printStackTrace);
        }
    }
    private double hesaplaBMR(User user) {
        double bmr;
        if (user.getGender().equalsIgnoreCase("kadın") || user.getGender().equalsIgnoreCase("k")) {
            bmr = 655.10 + (9.56 * user.getWeight()) + (1.85 * user.getHeight()) - (4.68 * user.getAge());
        } else {
            bmr = 66.47 + (13.75 * user.getWeight()) + (5 * user.getHeight()) - (6.76 * user.getAge());
        }
        return bmr;
    }

    private void loadSavedFoods(String date) {
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) return;

        //Kullanıcının yediği yiyecekleri db den çekme
        foodDatabase.foodDao().getByUserIdAndDate(userId,date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foods -> {
                    for (Food food : foods) {
                        switch (food.getMealType()) {
                            case "breakfast":
                                addBreakfastItem(food.getFoodName(), food.getCalorie());
                                break;
                            case "lunch":
                                addLunchItem(food.getFoodName(), food.getCalorie());
                                break;
                            case "dinner":
                                addDinnerItem(food.getFoodName(), food.getCalorie());
                                break;
                            case "snack":
                                addSnackItem(food.getFoodName(), food.getCalorie());
                                break;
                        }
                    }
                }, error -> {
                    Log.e("FoodDatabase", "Veriler yüklenirken hata: " + error.getMessage());
                });
    }
    private void getIntentInfo() {
        Intent intent = getIntent();
        foodName = intent.getStringExtra("foodName");
        foodCal = intent.getDoubleExtra("calorie", 0);
    }

    private void initalize() {
        //Buton
        kahvalti = findViewById(R.id.kahvalti);
        ogleYemegi = findViewById(R.id.ogleYemegi);
        aksamYemegi = findViewById(R.id.aksamYemegi);
        araOgun = findViewById(R.id.araOgun);
        homeBtn = findViewById(R.id.homeBtn);
        addFoodBtn = findViewById(R.id.addFoodBtn);

        //TextView
        txtBreakfastTotal = findViewById(R.id.txtBreakfastTotal);
        txtLunchTotal = findViewById(R.id.txtLunchTotal);
        txtDinnerTotal = findViewById(R.id.txtDinnerTotal);
        txtSnackTotal = findViewById(R.id.txtSnackTotal);

        totalCalorieTxt = findViewById(R.id.totalCalorieTxt);
        fazlaKaloriTxt = findViewById(R.id.fazlaKaloriTxt);

        //LinearLayout
        layoutBreakfastConetnt = findViewById(R.id.layoutBreakfastContent);
        layoutLunchContent = findViewById(R.id.layoutLunchContent);
        layoutDinnerContent = findViewById(R.id.layoutDinnerContent);
        layoutSnackContent = findViewById(R.id.layoutSnackContent);

        cardBreakfast = findViewById(R.id.cardBreakfast);
        cardLunch = findViewById(R.id.cardLunch);
        cardDinner = findViewById(R.id.cardDinner);
        cardSnack = findViewById(R.id.cardSnack);

        //items
        breakfastItems = findViewById(R.id.breakfastItems);
        lunchItems = findViewById(R.id.LunchItems);
        dinnerItems = findViewById(R.id.dinnerItems);
        snackItems = findViewById(R.id.snackItems);

        //yapılacaklar butonu
        yuruyus = findViewById(R.id.yuruyus);
        kosmak = findViewById(R.id.kosmak);
        bisikletsur = findViewById(R.id.bisikletsur);
        ipatlama = findViewById(R.id.ipatlama);

        //tarih
        dateTxt = findViewById(R.id.dateTxt);

        //terihler arası geçiş için
        gerigit = findViewById(R.id.gerigit);
        ilerigit = findViewById(R.id.ilerigit);
    }

    private void setListener() {
        //öğün butonları
        kahvalti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mealType = "breakfast";
                toggleMealSection(mealType);
            }
        });
        ogleYemegi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mealType = "lunch";
                toggleMealSection(mealType);
            }
        });
        aksamYemegi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mealType = "dinner";
                toggleMealSection(mealType);
            }
        });
        araOgun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mealType = "snack";
                toggleMealSection(mealType);
            }
        });

        //anasayfa ve yemek ekle butonları
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayDbFormat = dbFormat.format(new Date());

                //Bugünün tarihi değilse butonu pasif ve Toast göster
                if (!today.equals(todayDbFormat)) {
                    Toast.makeText(OgunTakip.this, "Güncel Tarihe Gelmeden Ana sayfaya Gidemezsiniz!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(OgunTakip.this,Anasayfa.class);
                startActivity(intent);
                finish();
            }
        });
        addFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayDbFormat = dbFormat.format(new Date());

                //Bugünün tarihi değilse butonu pasif ve Toast göster
                if (!today.equals(todayDbFormat)) {
                    Toast.makeText(OgunTakip.this, "Güncel Tarihe Gelmeden Yemek Ekleyemezsiniz!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(OgunTakip.this, KaloriHesapla.class);
                startActivity(intent);
                finish();
            }
        });


        //aktivite butonları
        yuruyus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sportType = "yuruyus";
                //seçilen aktiviteyi shared preferences e kaydediyor
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit().putString("selectedActivity", sportType).apply();

                Intent intent = new Intent(OgunTakip.this, ToDoList.class);
                intent.putExtra("selectedActivity", sportType); //todolist e seçilen aktiviteyi gönderiyor
                startActivity(intent);
            }
        });
        kosmak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sportType = "kosmak";
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit().putString("selectedActivity", sportType).apply();

                Intent intent = new Intent(OgunTakip.this, ToDoList.class);
                intent.putExtra("selectedActivity", sportType);
                startActivity(intent);
            }
        });
        bisikletsur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sportType = "bisikletsur";
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit().putString("selectedActivity", sportType).apply();

                Intent intent = new Intent(OgunTakip.this, ToDoList.class);
                intent.putExtra("selectedActivity", sportType);
                startActivity(intent);
            }
        });
        ipatlama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sportType = "ipatlama";
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit().putString("selectedActivity", sportType).apply();

                Intent intent = new Intent(OgunTakip.this, ToDoList.class);
                intent.putExtra("selectedActivity", sportType);
                startActivity(intent);
            }
        });

        //TARİH DEĞİŞTİRME TUŞLARI
        gerigit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.DAY_OF_MONTH, -1); // bir gün geri
                currentDate = cal.getTime();

                updateDate();
            }
        });
        ilerigit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.DAY_OF_MONTH, 1); // bir gün ileri

                Date todayDate = new Date();

                if (!cal.getTime().after(todayDate)) { // Bugünü geçme
                    currentDate = cal.getTime();
                    updateDate();
                }
            }
        });
    }
    private void toggleMealSection(String mealType) {
        if (mealType == null) return;

        switch (mealType) {
            case "breakfast":
                applyMealData(mealType);
                if (layoutBreakfastConetnt.getVisibility() == View.GONE){
                    layoutBreakfastConetnt.setVisibility(View.VISIBLE);
                }else {
                    layoutBreakfastConetnt.setVisibility(View.GONE);
                }
                layoutLunchContent.setVisibility(View.GONE);
                layoutDinnerContent.setVisibility(View.GONE);
                layoutSnackContent.setVisibility(View.GONE);
                break;
            case "lunch":
                applyMealData(mealType);
                if (layoutLunchContent.getVisibility() == View.GONE){
                    layoutLunchContent.setVisibility(View.VISIBLE);
                }else {
                    layoutLunchContent.setVisibility(View.GONE);
                }
                layoutBreakfastConetnt.setVisibility(View.GONE);
                layoutDinnerContent.setVisibility(View.GONE);
                layoutSnackContent.setVisibility(View.GONE);
                break;
            case "dinner":
                applyMealData(mealType);
                if (layoutDinnerContent.getVisibility() == View.GONE){
                    layoutDinnerContent.setVisibility(View.VISIBLE);
                }else {
                    layoutDinnerContent.setVisibility(View.GONE);
                }
                layoutBreakfastConetnt.setVisibility(View.GONE);
                layoutLunchContent.setVisibility(View.GONE);
                layoutSnackContent.setVisibility(View.GONE);
                break;
            case "snack":
                applyMealData(mealType);
                if (layoutSnackContent.getVisibility() == View.GONE){
                    layoutSnackContent.setVisibility(View.VISIBLE);
                }else{
                    layoutSnackContent.setVisibility(View.GONE);
                }
                layoutBreakfastConetnt.setVisibility(View.GONE);
                layoutLunchContent.setVisibility(View.GONE);
                layoutDinnerContent.setVisibility(View.GONE);
        }
    }
    private void applyMealData(String mealType) {
        if (mealType == null) return;

        // intentten veri gelmediyse ekleme yapılmaz
        if (foodName == null || foodName.trim().isEmpty()) return;

        //Bugünün tarihine ekleme yapmak için
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDbFormat = dbFormat.format(new Date()); // bugünün tarihi

        if (!today.equals(todayDbFormat)) {
            Toast.makeText(this, "Sadece bugünün yemeğini ekleyebilirsiniz!", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (mealType) {
            case "breakfast":
                breakfastItems.setVisibility(View.VISIBLE);
                addBreakfastItem(foodName, foodCal);
                addfoodDatabase(mealType);
                break;
            case "lunch":
                lunchItems.setVisibility(View.VISIBLE);
                addLunchItem(foodName, foodCal);
                addfoodDatabase(mealType);
                break;
            case "dinner":
                dinnerItems.setVisibility(View.VISIBLE);
                addDinnerItem(foodName, foodCal);
                addfoodDatabase(mealType);
                break;
            case "snack":
                snackItems.setVisibility(View.VISIBLE);
                addSnackItem(foodName, foodCal);
                addfoodDatabase(mealType);
                break;
        }
    }

    private void addfoodDatabase(String mealType) {
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this,"Önce giriş yapın", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Food food = new Food(userId, foodName, foodCal, mealType,this.today);

        foodDatabase.foodDao().insert(food)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        //Database kontrolü
        /*foodDatabase.foodDao().insert(food)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> Log.d("FoodDatabase", "Yemek eklendi → "
                                + "userId=" + userId
                                + ", mealType=" + mealType
                                + ", foodName=" + foodName
                                + ", calorie=" + foodCal
                                + ", date=" + today
                                + ", rowId=" + id),
                        error -> Log.e("FoodDatabase", "Ekleme hatası: " + error.getMessage())
                );*/
    }

    private void addBreakfastItem(String name, double cal) {
        if (name == null || name.trim().isEmpty()) return;
        if (breakfastList.contains(name)) return;

        TextView item = new TextView(this);
        item.setText(name + "  -  " + formatCalorie(cal));
        item.setTextSize(16);
        item.setTextColor(0xFF000000);
        item.setPadding(8, 8, 8, 8);

        breakfastItems.addView(item);
        Food food = new Food(userId, foodName, foodCal, mealType,today);
        breakfastList.add(food);

        double currentTotal = parseCalorieFromText(txtBreakfastTotal.getText().toString());
        txtBreakfastTotal.setText(formatCalorie(currentTotal + cal));

        updateTotalCalories();
    }

    private void addLunchItem(String name, double cal) {
        if (name == null || name.trim().isEmpty()) return;
        if (lunchList.contains(name)) return;

        TextView item = new TextView(this);
        item.setText(name + "  -  " + formatCalorie(cal));
        item.setTextSize(16);
        item.setTextColor(0xFF000000);
        item.setPadding(8, 8, 8, 8);

        lunchItems.addView(item);
        Food food = new Food(userId, foodName, foodCal, mealType,today);
        lunchList.add(food);

        double currentTotal = parseCalorieFromText(txtLunchTotal.getText().toString());
        txtLunchTotal.setText(formatCalorie(currentTotal + cal));

        updateTotalCalories();
    }
    private void addDinnerItem(String name, double cal) {
        if (name == null || name.trim().isEmpty()) return;
        if (dinnerList.contains(name)) return;

        TextView item = new TextView(this);
        item.setText(name + "  -  " + formatCalorie(cal));
        item.setTextSize(16);
        item.setTextColor(0xFF000000);
        item.setPadding(8, 8, 8, 8);

        dinnerItems.addView(item);

        Food food = new Food(userId, foodName, foodCal, mealType,today);
        dinnerList.add(food);

        double currentTotal = parseCalorieFromText(txtDinnerTotal.getText().toString());
        txtDinnerTotal.setText(formatCalorie(currentTotal + cal));

        updateTotalCalories();
    }

    private void addSnackItem(String name, double cal) {
        if (name == null || name.trim().isEmpty()) return;
        if (snackList.contains(name)) return;

        TextView item = new TextView(this);
        item.setText(name + "  -  " + formatCalorie(cal));
        item.setTextSize(16);
        item.setTextColor(0xFF000000);
        item.setPadding(8, 8, 8, 8);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        snackItems.addView(item);

        Food food = new Food(userId, foodName, foodCal, mealType,today);
        snackList.add(food);

        double currentTotal = parseCalorieFromText(txtSnackTotal.getText().toString());
        txtSnackTotal.setText(formatCalorie(currentTotal + cal));

        updateTotalCalories();
    }

    private void updateTotalCalories() {
        double breakfastTotal = parseCalorieFromText(txtBreakfastTotal.getText().toString());
        double lunchTotal = parseCalorieFromText(txtLunchTotal.getText().toString());
        double dinnerTotal = parseCalorieFromText(txtDinnerTotal.getText().toString());
        double snackTotal = parseCalorieFromText(txtSnackTotal.getText().toString());

        double total = breakfastTotal + lunchTotal + dinnerTotal + snackTotal;
        totalCalorieTxt.setText(String.format(Locale.getDefault(), "%.2f", total) +"/" + String.format(Locale.getDefault(), "%.0f", bmr) + " kCal");
        if (total > bmr){
            fazlaKaloriTxt.setVisibility(View.VISIBLE);
            fazlaKaloriTxt.setText("Fazla Alınan Kalori: " + String.format(Locale.getDefault(), "%.2f kcal", total - bmr));
        }else {
            fazlaKaloriTxt.setVisibility(View.INVISIBLE);
        }
    }

    private double parseCalorieFromText(String text) {
        if (text == null) {
            return 0;
        }

        try {
            String value = text.replace("kcal", "").trim();
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private String formatCalorie(double calorie) {
        return String.format(Locale.getDefault(), "%.2f kcal", calorie);
    }

    private void updateDate() {
        sdf = new SimpleDateFormat("dd MMMM", new Locale("tr", "TR"));
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        //Tarihleri formatla
        String displayDate = sdf.format(currentDate);
        today = dbFormat.format(currentDate);

        dateTxt.setText(displayDate);

        //Günlük verileri temizle
        clearDailyData();

        //TextView sıfırla
        totalCalorieTxt.setText(String.format(Locale.getDefault(), "0/%.0f kcal", bmr));
        fazlaKaloriTxt.setVisibility(View.INVISIBLE);

        loadSavedFoods(today);
    }


    private void clearDailyData() {
        // Listeleri temizle
        breakfastList.clear();
        lunchList.clear();
        dinnerList.clear();
        snackList.clear();

        // LinearLayout içindeki tüm öğeleri temizle
        breakfastItems.removeAllViews();
        lunchItems.removeAllViews();
        dinnerItems.removeAllViews();
        snackItems.removeAllViews();

        // Toplam kalorileri sıfırla
        txtBreakfastTotal.setText("0 kcal");
        txtLunchTotal.setText("0 kcal");
        txtDinnerTotal.setText("0 kcal");
        txtSnackTotal.setText("0 kcal");

        // Genel toplam ve fazla kalori
        totalCalorieTxt.setText("0/" + String.format(Locale.getDefault(), "%.0f", bmr) + " kcal");
        fazlaKaloriTxt.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int userId = sharedPreferences.getInt("userId", -1);
        if(userId != -1){
            foodDatabase.foodDao().getByUserIdAndDate(userId, today)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(foods -> {

                        double total = 0;

                        for (Food food : foods) {
                            total += food.getCalorie();
                        }

                        totalCalorieTxt.setText(
                                String.format(Locale.getDefault(),"%.0f/%.0f kcal", total, bmr)
                        );

                        if (total > bmr){
                            fazlaKaloriTxt.setVisibility(View.VISIBLE);
                            fazlaKaloriTxt.setText("Fazla Alınan Kalori: " + String.format(Locale.getDefault(), "%.2f kcal", total - bmr)
                            );
                        } else {
                            fazlaKaloriTxt.setVisibility(View.INVISIBLE);
                        }

                    }, Throwable::printStackTrace);
        }

        //Bugünün tarihini al
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDbFormat = dbFormat.format(new Date());

        //Eğer eski güne geçilmişse addFoodBtn kapalı
        if (!today.equals(todayDbFormat)) {
            addFoodBtn.setEnabled(false);
        } else {
            addFoodBtn.setEnabled(true);
        }

        loadSavedFoods(today);
    }

}