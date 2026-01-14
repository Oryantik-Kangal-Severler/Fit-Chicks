package com.gulseren.fitchicks.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gulseren.fitchicks.R;
import com.gulseren.fitchicks.database.AppDatabase;
import com.gulseren.fitchicks.database.SportActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ToDoList extends AppCompatActivity {

    TextView textBaslik, textSonuc;
    EditText editKalori;
    Button btnHesapla, btnEkle;
    ListView listViewTasks;

    ArrayList<String> yapilanSporlarListesi;
    ArrayList<SportActivity> sportActivityList;
    ArrayAdapter<String> adapter;

    String gelenSporTuru = "";
    double kullaniciKilosu = 0;
    AppDatabase appDatabase, sportDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todolist);

        initViews();

        // Veritabanını güvenli başlat
        try {
            appDatabase = AppDatabase.getInstance(this);
            sportDatabase = AppDatabase.getInstance(this);
            getUserWeight();
        } catch (Exception e) {
            Log.e("ToDoListError", "Veritabanı hatası: " + e.getMessage());
        }

        getIntentData();
        yapilanSporlarListesi = new ArrayList<>();
        sportActivityList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, yapilanSporlarListesi);
        listViewTasks.setAdapter(adapter);

        loadDatabaseData();

        setupListView();
        setupListeners();
    }

    private void setupListView() {
        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            // Pozisyon geçerli mi kontrol et
            if (position < 0 || position >= sportActivityList.size()) return true;

            new AlertDialog.Builder(ToDoList.this)
                    .setTitle("Aktivite Yapıldı mı?")
                    .setMessage("Aktiviteyi Yaptınız mı?")
                    .setPositiveButton("Yaptım", (dialog, which) -> {
                        // DB’den ve listeden silme
                        SportActivity activityToDelete = sportActivityList.get(position);

                        new Thread(() -> sportDatabase.sportDao().deleteActivity(activityToDelete.getId())).start();

                        // Listelerden sil
                        yapilanSporlarListesi.remove(position);
                        sportActivityList.remove(position);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(ToDoList.this, "Silindi", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Yapmadım", null)
                    .show();
            return true;
        });
    }

    private void loadDatabaseData() {

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sp.getInt("userId", -1);
        if (userId == -1) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        sportDatabase.sportDao().getByUserIdAndDate(userId, today)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activities -> {

                    yapilanSporlarListesi.clear();
                    sportActivityList.clear();

                    sportActivityList.addAll(activities);

                    for (SportActivity s : activities) {
                        String item =
                                s.getSportActivity() + " - " +
                                        s.getCalorie() + " kcal - " +
                                        s.getTime();
                        yapilanSporlarListesi.add(item);
                    }

                    adapter.notifyDataSetChanged();

                }, Throwable::printStackTrace);
    }


    private void initViews() {
        textBaslik = findViewById(R.id.textView);
        textSonuc = findViewById(R.id.textView2);
        editKalori = findViewById(R.id.editText);
        btnHesapla = findViewById(R.id.hesaplabuttonu);
        btnEkle = findViewById(R.id.eklebutonu);
        listViewTasks = findViewById(R.id.listViewTasks);
    }

    private void getUserWeight() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            appDatabase.userDao().getById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                        if (user != null) {
                            kullaniciKilosu = user.getWeight();
                            // Kilo geldi mi diye log atıyoruz
                            Log.d("ToDoList", "Kullanıcı kilosu alındı: " + kullaniciKilosu);
                        }
                    }, throwable -> {
                        Log.e("ToDoList", "Kilo alma hatası: " + throwable.getMessage());
                    });
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        gelenSporTuru = intent.getStringExtra("selectedActivity");

        if (gelenSporTuru != null) {
            switch (gelenSporTuru) {
                case "yuruyus":
                    textBaslik.setText("Yürüyüş");
                    break;
                case "kosmak":
                    textBaslik.setText("Koşu");
                    break;
                case "bisikletsur":
                    textBaslik.setText("Bisiklet Sürme");
                    break;
                case "ipatlama":
                    textBaslik.setText("İp Atlama");
                    break;
                default:
                    textBaslik.setText(gelenSporTuru);
                    break;
            }
        }else {
            textBaslik.setText("Aktivite");
        }
    }

    private void setupListeners() {
        //HESAPLA BUTONU
        btnHesapla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String girilenDeger = editKalori.getText().toString().trim(); // Boşlukları temizle

                    if (girilenDeger.isEmpty()) {
                        Toast.makeText(ToDoList.this, "Lütfen yakılacak kaloriyi giriniz!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (kullaniciKilosu == 0) {
                        Toast.makeText(ToDoList.this, "Kullanıcı kilosu yükleniyor veya bulunamadı, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
                        // Kilo gelmediyse tekrar çekmeyi deneyelim
                        getUserWeight();
                        return;
                    }

                    // Virgül hatasını önlemek için virgülü noktaya çeviriyoruz
                    if (girilenDeger.contains(",")) {
                        girilenDeger = girilenDeger.replace(",", ".");
                    }

                    double kalori = Double.parseDouble(girilenDeger);
                    double sureSaat = 0;

                    if (gelenSporTuru == null) {
                        Toast.makeText(ToDoList.this, "Spor türü seçilmedi!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    switch (gelenSporTuru) {
                        case "yuruyus":
                            sureSaat = kalori / (2.2 * kullaniciKilosu);
                            break;
                        case "kosmak":
                            sureSaat = kalori / (6 * kullaniciKilosu);
                            break;
                        case "bisikletsur":
                            sureSaat = kalori / (6 * kullaniciKilosu);
                            break;
                        case "ipatlama":
                            sureSaat = kalori / (8.8 * kullaniciKilosu);
                            break;
                        case "Aktivite Listem":
                            Toast.makeText(ToDoList.this, "Ana sayfadan girişte hesaplama yapılmaz.", Toast.LENGTH_SHORT).show();
                            return;
                        default:
                            // Bilinmeyen spor türü durumunda varsayılan bir hesap veya uyarı
                            Toast.makeText(ToDoList.this, "Bu spor için formül tanımlı değil.", Toast.LENGTH_SHORT).show();
                            return;
                    }

                    int sureDakika = (int) (sureSaat * 60);
                    textSonuc.setText("Gereken Süre: " + sureDakika + " Dakika");

                } catch (NumberFormatException e) {
                    // Kullanıcı sayı yerine harf girdiyse veya format bozuksa
                    Toast.makeText(ToDoList.this, "Lütfen geçerli bir sayı giriniz!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Diğer tüm hatalar için
                    Log.e("HesaplaHatası", "Hata detayı: " + e.getMessage());
                    Toast.makeText(ToDoList.this, "Bir hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // --- EKLE BUTONU ---
        btnEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String sonucMetni = textSonuc.getText().toString();
                    String girilenKalori = editKalori.getText().toString().trim();
                    double kalori = Double.parseDouble(girilenKalori);

                    if (!sonucMetni.isEmpty() && !sonucMetni.contains("Sonuç") && !girilenKalori.isEmpty()) {

                        String sporAdi = textBaslik.getText().toString();
                        String sureKisimi = sonucMetni.replace("Gereken Süre: ", "");

                        String listeElemani = sporAdi + " - " + girilenKalori + " kcal - " + sureKisimi;

                        yapilanSporlarListesi.add(listeElemani);
                        adapter.notifyDataSetChanged();

                        addDatabase(kalori, sureKisimi);

                        //MainActivity için bilgi gönderme
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("yeniAktivite", listeElemani);

                        setResult(RESULT_OK, resultIntent);  // ← BUNU EKLEMEN GEREKİYOR
                        finish(); // ← ekranı kapat ve geri dön


                        editKalori.setText("");
                        textSonuc.setText("Eklemek istediğin başka bir şey var mı?"); // Sıfırla
                        Toast.makeText(ToDoList.this, "Listeye Eklendi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ToDoList.this, "Önce geçerli bir hesaplama yapmalısınız!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ToDoList.this, "Ekleme sırasında hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addDatabase(double calorie, String sure) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) return;

        SportActivity sportActivity = new SportActivity(userId, textBaslik.getText().toString(),calorie,sure,today);
        sportDatabase.sportDao().insert(sportActivity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> Log.d("ToDoList", "Kayıt eklendi, ID: " + id),
                        error -> Log.e("ToDoList", "Kayıt ekleme hatası: " + error.getMessage())
                );

                //Database kontrolü
                /*.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> Log.d("ToDoList", "Eklendi → "
                                + "userId=" + userId
                                + ", sportActivity=" + textBaslik.getText().toString()
                                + ", calorie=" + calorie
                                + ", sure=" + sure
                                + ", date=" + today),
                        error -> Log.e("ToDoList", "Ekleme hatası: " + error.getMessage())
                );*/
    }
}