package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DBCities dbCities;
    private CityAdapter cityAdapter;

    private Button buttonHeatCalculator;
    private Button buttonFindCity;
    private Button buttonFindCityByCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCitiesList();
        initButtons();
    }

    private void initButtons() {
        buttonHeatCalculator = findViewById(R.id.btnHeatCalculator);
        buttonFindCity = findViewById(R.id.btnFindCity);
        buttonFindCityByCoordinates = findViewById(R.id.btnFindCityByCoordinates);

        buttonHeatCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(this, HeatActivity.class);
            startActivity(intent);
        });

        buttonFindCity.setOnClickListener(v -> {
            showCitySearchDialog();
        });
        buttonFindCityByCoordinates.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void initCitiesList() {
        OnCityClickListener onCityClick = (city, position) -> {
            sendPOST(city, cityAdapter);
            city.setDateTime(LocalDateTime.now());
        };

        RecyclerView recyclerView = findViewById(R.id.list);

        dbCities = new DBCities(this);
        cityAdapter = new CityAdapter(this, dbCities.selectAll(), onCityClick);

        recyclerView.setAdapter(cityAdapter);
    }

    private void updateList() {
        cityAdapter.setCities(dbCities.selectAll());
        cityAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            City city = (City) data.getExtras().getSerializable(AddActivity.CITY_CODE);
            dbCities.insert(city.getName(), city.getTemp(), city.getLatitude(), city.getLongtitude(), city.getFlagResource(), city.getDateTime());
            updateList();
        }
    }

    public void sendPOST(City city, CityAdapter adapter) {
        if (!isNetworkAvailable()) {
            runOnUiThread(() -> Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String forecastAddress = this.getString(R.string.forecast_address);

        try {
            HttpUrl url = HttpUrl.parse(forecastAddress).newBuilder()
                    .addQueryParameter(getString(R.string.latitude), city.getLatitude())
                    .addQueryParameter(getString(R.string.longitude), city.getLongtitude())
                    .addQueryParameter(getString(R.string.current_weather), "true")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        MainActivity.this.runOnUiThread(() -> {
                            city.setTemp(getString(R.string.coordinates_error));
                            adapter.notifyDataSetChanged();
                            dbCities.update(city);
                        });
                    } else {
                        try {
                            setTemperature(city, response);
                        } catch (JSONException jsonException) {
                            Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    MainActivity.this.runOnUiThread(() -> {
                        city.setTemp(String.valueOf(R.string.err_connect));
                        adapter.notifyDataSetChanged();
                        dbCities.update(city);
                    });

                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setTemperature(City city, Response response) throws IOException, JSONException {
        final String responseData = response.body().string();
        String temperatureFromApi = new JSONObject(responseData)
                .getJSONObject(getString(R.string.current_weather))
                .get(getString(R.string.temperature))
                .toString();

        MainActivity.this.runOnUiThread(() -> {
            city.setTemp(temperatureFromApi);
            cityAdapter.notifyDataSetChanged();
            dbCities.update(city);
        });
    }

    private void showCitySearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText cityNameInput = new EditText(this);

        builder
                .setTitle("Найти город")
                .setView(cityNameInput)
                .setPositiveButton("Найти", (dialog, which) -> {
                    String cityName = cityNameInput.getText().toString();
                    if (!cityName.isEmpty()) {
                        searchCityCoordinates(cityName);
                    } else {
                        Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void searchCityCoordinates(String cityName) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + cityName;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "NevermorePayForWater/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Ошибка поиска: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() > 0) {
                        JSONObject result = jsonArray.getJSONObject(0);

                        double latitude = result.getDouble("lat");
                        double longitude = result.getDouble("lon");

                        runOnUiThread(() -> {
                            Intent intent = new Intent(MainActivity.this, AddActivity.class);
                            intent.putExtra(AddActivity.CITY_NAME_CODE, cityName);
                            intent.putExtra(AddActivity.LATITUDE_CODE, latitude);
                            intent.putExtra(AddActivity.LONGITUDE_CODE, longitude);
                            startActivity(intent);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Город не найден", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}