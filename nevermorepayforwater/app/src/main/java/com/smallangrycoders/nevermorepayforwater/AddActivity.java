package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddActivity extends Activity {
    private Button butFindCoords, btSave, btCancel;
    private EditText etCityName, etLat, etLon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        // Находим вьюхи
        etCityName   = findViewById(R.id.City);      // ваше поле для названия города
        etLat        = findViewById(R.id.etLat);
        etLon        = findViewById(R.id.etLon);
        butFindCoords= findViewById(R.id.butFindCoords);
        btSave       = findViewById(R.id.butSave);
        btCancel     = findViewById(R.id.butCancel);

        // Слушатель кнопки "Найти координаты"
        butFindCoords.setOnClickListener(v -> {
            String city = etCityName.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            } else {
                fetchCoordinates(city);
            }
        });

        // Сохранить — вернём StCity в MainActivity
        btSave.setOnClickListener(v -> {
            String name = etCityName.getText().toString();
            String lat  = etLat.getText().toString();
            String lon  = etLon.getText().toString();

            // Можно добавить валидацию: что lat/lon непустые
            if (lat.isEmpty() || lon.isEmpty()) {
                Toast.makeText(this, "Сначала получите координаты", Toast.LENGTH_SHORT).show();
                return;
            }

            StCity stcity = new StCity(
                    -1,
                    name,
                    "0",
                    lat,
                    lon,
                    1,
                    LocalDateTime.now()
            );
            Intent intent = getIntent();
            intent.putExtra("StCity", stcity);
            setResult(RESULT_OK, intent);
            finish();
        });

        // Отмена
        btCancel.setOnClickListener(v -> finish());
    }

    /**
     * Делаем запрос к OpenCage Geocoding API, парсим lat/lng и
     * заполняем etLat, etLon.
     */
    private void fetchCoordinates(String cityName) {
        OkHttpClient client = new OkHttpClient();
        String apiKey = getString(R.string.opencage_key);

        String url;
        try {
            url = "https://api.opencagedata.com/geocode/v1/json"
                    + "?q=" + URLEncoder.encode(cityName, "UTF-8")
                    + "&key=" + apiKey;
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка формирования запроса", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddActivity.this,
                                "Не удалось подключиться к сервису",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(AddActivity.this,
                                    "Сервис вернул ошибку " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject root    = new JSONObject(body);
                    JSONArray results  = root.getJSONArray("results");
                    if (results.length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(AddActivity.this,
                                        "Город не найден",
                                        Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }
                    JSONObject geom = results
                            .getJSONObject(0)
                            .getJSONObject("geometry");
                    final String lat = geom.getString("lat");
                    final String lng = geom.getString("lng");

                    runOnUiThread(() -> {
                        etLat.setText(lat);
                        etLon.setText(lng);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
