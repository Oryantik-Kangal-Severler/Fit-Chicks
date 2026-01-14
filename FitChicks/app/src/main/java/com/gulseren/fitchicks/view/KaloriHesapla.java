package com.gulseren.fitchicks.view;

import static kotlinx.coroutines.flow.FlowKt.subscribeOn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.gulseren.fitchicks.BuildConfig;
import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.services.FoodAPI;
import com.gulseren.fitchicks.services.GoogleTranslateAPI;
import com.gulseren.fitchicks.database.AppDatabase;
import com.gulseren.fitchicks.model.FoodModel;
import com.gulseren.fitchicks.model.FoodResponse;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class KaloriHesapla extends AppCompatActivity {
    String CALORIE_NINJA_API_KEY ="mAFBjICU6ZrPxY51KSdyQQ==76TbupJbxOe8ugfI";
    Button searchButton, backButton, addMealButton;
    TextView calorieTextView;
    EditText searchFoodText;

    String foodName;
    private double currentCalorie = 0;

    ArrayList<FoodModel> foodList;
    private String BASE_URL = "https://api.calorieninjas.com/v1/";
    private String TRANSLATE_BASE_URL = "https://translation.googleapis.com/language/translate/v2/";

    AppDatabase database;
    Retrofit retrofit;
    Retrofit translateRetrofit;


    CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kalorihesapla);

        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.backButton);
        addMealButton = findViewById(R.id.addMealButton);
        calorieTextView = findViewById(R.id.calorieTextView);
        searchFoodText = findViewById(R.id.searchFoodText);

        addMealButton.setEnabled(false);

        database = AppDatabase.getInstance(this);

        //retrofit
        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();

        translateRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(TRANSLATE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodName = searchFoodText.getText().toString();
                if (foodName.isEmpty()) {
                    Toast.makeText(KaloriHesapla.this, "Önce Yemek Giriniz", Toast.LENGTH_SHORT).show();
                } else {
                    calorieTextView.setText("Aranıyor...");
                    translateData(foodName);
                }
                backButton.setEnabled(true);
                addMealButton.setEnabled(true);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KaloriHesapla.this, Anasayfa.class);
                startActivity(intent);
                finish();
            }
        });

        addMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodName = searchFoodText.getText().toString();
                if (foodName.isEmpty()) {
                    Toast.makeText(KaloriHesapla.this,"Yemek Adını Giriniz", Toast.LENGTH_SHORT).show();
                    return;
                } else if (currentCalorie == 0) {
                    Toast.makeText(KaloriHesapla.this,"Lütfen Yemek Arayın", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(KaloriHesapla.this, OgunTakip.class);
                intent.putExtra("foodName", foodName);
                intent.putExtra("calorie", currentCalorie);
                startActivity(intent);
                finish();
            }
        });
    }
    private void translateData(String foodName) {
        String translateApiKey = BuildConfig.GCP_TRANSLATE_API_KEY;

        if (translateApiKey != null && !translateApiKey.trim().isEmpty()) {
            GoogleTranslateAPI googleTranslateAPI = translateRetrofit.create(GoogleTranslateAPI.class);
            compositeDisposable.add(
                    googleTranslateAPI.translate(foodName, "en", "tr", translateApiKey)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                String translated = foodName;
                                if (response != null && response.getData() != null &&
                                        response.getData().getTranslations() != null &&
                                        !response.getData().getTranslations().isEmpty() &&
                                        response.getData().getTranslations().get(0).getTranslatedText() != null) {
                                    translated = response.getData().getTranslations().get(0).getTranslatedText();
                                }
                                // Çeviri başarılı olsa da olmasa da arama yap
                                loadData(translated);
                            },
                                    error -> {
                                        // Çeviri hatası - doğrudan Türkçe metinle arama yap
                                        error.printStackTrace();
                                        loadData(foodName);
                                    })
            );

        } else {
            // API anahtarı yoksa doğrudan arama yap
            loadData(foodName);
        }
    }

    public void loadData(String query) {
        final FoodAPI foodAPI = retrofit.create(FoodAPI.class);

        compositeDisposable.add(foodAPI.getData(query, BuildConfig.CALORIE_NINJAS_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> handleResponse(response),
                        error -> handleError(error)));
    }
    
    private void handleError(Throwable error) {
        error.printStackTrace();
        String errorMessage = "Hata: ";
        if (error.getMessage() != null) {
            if (error.getMessage().contains("404") || error.getMessage().contains("Not Found")) {
                errorMessage = "Yemek bulunamadı. Lütfen başka bir arama yapın.";
            } else if (error.getMessage().contains("401") || error.getMessage().contains("Unauthorized")) {
                errorMessage = "API anahtarı geçersiz.";
            } else if (error.getMessage().contains("network") || error.getMessage().contains("timeout")) {
                errorMessage = "İnternet bağlantısı hatası. Lütfen tekrar deneyin.";
            } else {
                errorMessage = "Hata: " + error.getMessage();
            }
        } else {
            errorMessage = "Bilinmeyen bir hata oluştu.";
        }
        
        calorieTextView.setText(errorMessage);
        //currentCalorie = 0;
        addMealButton.setEnabled(false);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleResponse(FoodResponse response) {
        if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
            foodList = new ArrayList<FoodModel>(response.getItems());

            FoodModel food = response.getItems().get(0);
            currentCalorie = food.getCalories();
            calorieTextView.setText("Kalori: " + currentCalorie + " kcal");

            addMealButton.setEnabled(true);
            backButton.setEnabled(true);
        } else {
            // Response boş veya items yok
            calorieTextView.setText("Yemek bulunamadı. Lütfen başka bir arama terimi deneyin.");
            currentCalorie = 0;
            addMealButton.setEnabled(false);
            Toast.makeText(this, "Yemek bilgisi bulunamadı", Toast.LENGTH_SHORT).show();
        }
    }
}
