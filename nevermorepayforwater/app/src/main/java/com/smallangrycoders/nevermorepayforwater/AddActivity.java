package com.smallangrycoders.nevermorepayforwater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddActivity extends Activity {
    private EditText etLoc;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        Button btSave = findViewById(R.id.butSave);
        Button btCancel = findViewById(R.id.butCancel);
        etLoc = findViewById(R.id.City);

        btSave.setOnClickListener(v -> {
            String cityName = etLoc.getText().toString();
            if (cityName.isEmpty()) {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchCoordinates(cityName);
        });

        btCancel.setOnClickListener(v -> finish());
    }

    private void fetchCoordinates(String cityName) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + cityName + "&format=json&limit=1";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "YourAppNameHere")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddActivity.this, "Ошибка геокодирования", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String respStr = response.body().string();
                try {
                    JSONArray arr = new JSONArray(respStr);
                    if (arr.length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(AddActivity.this, "Город не найден", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    JSONObject obj = arr.getJSONObject(0);
                    String lat = obj.getString("lat");
                    String lon = obj.getString("lon");

                    StCity stcity = new StCity(-1, cityName, "0", lat, lon, 1, LocalDateTime.now());
                    @SuppressLint("UnsafeIntentLaunch") Intent intent = getIntent();
                    intent.putExtra("StCity", stcity);
                    setResult(RESULT_OK, intent);
                    finish();

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AddActivity.this, "Ошибка обработки ответа", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
