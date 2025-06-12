package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
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
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DBCities stcConnector;
    private ArrayList<StCity> states = new ArrayList<StCity>();
    private StCityAdapter adapter;
    private Button btnHeatCalculator, btnFindCity;
    private Context oContext;

    private final int ADD_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.list);
        oContext = this;
        stcConnector = new DBCities(this);
        adapter = new StCityAdapter(this, stcConnector.selectAll(), null, oContext);

        StCityAdapter.OnStCityClickListener stateClickListener = (state, position) -> {
            sendPOST(state, adapter);
            state.setSyncDate(LocalDateTime.now());
        };

        adapter.SetOnCl(stateClickListener);
        recyclerView.setAdapter(adapter);

        btnHeatCalculator = findViewById(R.id.btnHeatCalculator);
        btnFindCity = findViewById(R.id.btnFindCity);

        btnHeatCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(this, HeatActivity.class);
            startActivity(intent);
        });

        btnFindCity.setOnClickListener(v -> showCitySearchDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void updateList() {
        adapter.setArrayMyData(stcConnector.selectAll());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent i = new Intent(this, AddActivity.class);
                startActivityForResult(i, ADD_ACTIVITY);
                return true;
            case R.id.deleteAll:
                stcConnector.deleteAll();
                updateList();
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            StCity st = (StCity) data.getExtras().getSerializable("StCity");
            stcConnector.insert(st.getName(), st.getTemp(), st.getLatitude(), st.getLongtitude(), st.getFlagResource(), st.getSyncDate());
            updateList();

        }
    }

    public void sendPOST(StCity state, StCityAdapter adapter) {
        if (!isNetworkAvailable()) {
            runOnUiThread(() -> Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String forecastAddress = this.getString(R.string.forecast_address);

        try {
            HttpUrl url = HttpUrl.parse(forecastAddress).newBuilder()
                    .addQueryParameter(getString(R.string.latitude), state.getLatitude())
                    .addQueryParameter(getString(R.string.longitude), state.getLongtitude())
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
                            state.setTemp(getString(R.string.coordinates_error));
                            adapter.notifyDataSetChanged();
                            stcConnector.update(state);
                        });
                    } else {
                        final String responseData = response.body().string();
                        JSONObject jo;
                        try {
                            jo = new JSONObject(responseData);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String tempFromAPI;
                        try {
                            tempFromAPI = jo.getJSONObject(getString(R.string.current_weather)).get(getString(R.string.temperature)).toString();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        MainActivity.this.runOnUiThread(() -> {
                            state.setTemp(tempFromAPI);
                            adapter.notifyDataSetChanged();
                            stcConnector.update(state);
                        });
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    MainActivity.this.runOnUiThread(() -> {
                        state.setTemp(String.valueOf(R.string.err_connect));
                        adapter.notifyDataSetChanged();
                        stcConnector.update(state);
                    });

                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showCitySearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Найти город");
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Найти", (dialog, which) -> {
            String cityName = input.getText().toString();
            if (!cityName.isEmpty()) {
                searchCityCoordinates(cityName);
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
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
                        JSONObject firstResult = jsonArray.getJSONObject(0);
                        double lat = firstResult.getDouble("lat");
                        double lon = firstResult.getDouble("lon");

                        runOnUiThread(() -> {
                            Intent intent = new Intent(MainActivity.this, AddActivity.class);
                            intent.putExtra("lat", lat);
                            intent.putExtra("lon", lon);
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