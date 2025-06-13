package com.smallangrycoders.nevermorepayforwater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DBCities stcConnector;
    private Context oContext;
    private StCityAdapter adapter;
    private int pendingUpdates = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            initializeComponents();
            setupRecyclerView();

        } catch (Exception e) {
            handleCriticalError(getString(R.string.err_text), e);
        }
    }

    private void initializeComponents() {
        try {
            oContext = this;
            stcConnector = new DBCities(this);
        } catch (Exception e) {
            throw new RuntimeException(getString(R.string.err_text), e);
        }
    }

    private void setupRecyclerView() {
        try {
            RecyclerView recyclerView = findViewById(R.id.list);
            if (recyclerView == null) {
                throw new IllegalStateException(getString(R.string.err_text));
            }

            ArrayList<StCity> cities = safeGetCities();
            adapter = new StCityAdapter(this, cities, null, oContext);

            StCityAdapter.OnStCityClickListener stateClickListener = (state, position) -> {
                try {
                    if (state != null) {
                        sendPOST(state, adapter);
                        state.setSyncDate(LocalDateTime.now());
                    }
                } catch (Exception e) {
                    Log.e(TAG, getString(R.string.err_text), e);
                }
            };

            adapter.SetOnCl(stateClickListener);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            throw new RuntimeException(getString(R.string.err_text), e);
        }
    }

    private ArrayList<StCity> safeGetCities() {
        try {
            ArrayList<StCity> cities = stcConnector.selectAll();
            return cities != null ? cities : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            return false;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateList() {
        try {
            ArrayList<StCity> cities = safeGetCities();
            adapter.setArrayMyData(cities);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            showToast(getString(R.string.err_text));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {

            switch (item.getItemId()) {
                case R.id.add:
                    startAddActivity();
                    return true;
                case R.id.deleteAll:
                    deleteAllCities();
                    return true;
                case R.id.exit:
                    finish();
                    return true;
                case R.id.refreshAll:
                    refreshAllCities();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            return false;
        }
    }

    /** @noinspection deprecation*/
    private void startAddActivity() {
        try {
            int ADD_ACTIVITY = 0;
            startActivityForResult(new Intent(this, AddActivity.class), ADD_ACTIVITY);
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            showToast(getString(R.string.err_text));
        }
    }

    private void deleteAllCities() {
        try {
            stcConnector.deleteAll();
            updateList();
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            showToast(getString(R.string.err_text));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                processActivityResult(data);
            }
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            showToast(getString(R.string.err_text));
        }
    }

    private void processActivityResult(Intent data) {
        try {
            Bundle extras = data.getExtras();
            if (extras == null) return;

            StCity st = (StCity) extras.getSerializable("StCity");
            if (st == null) return;

            DBCities.insert(st.getName(), st.getTemp(), st.getStrLat(),
                    st.getStrLon(), st.getFlagResource(), st.getSyncDate());
            updateList();
        } catch (Exception e) {
            throw new RuntimeException(getString(R.string.err_text), e);
        }
    }

    public void sendPOST(StCity state, StCityAdapter adapter) {
        if (state == null || adapter == null) {
            Log.w(TAG, getString(R.string.err_text));
            return;
        }

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = buildWeatherRequest(state);
            client.newCall(request).enqueue(new WeatherCallback(state, adapter));
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            updateCityWithError(state, adapter, getString(R.string.err_connect));
        }
    }

    private Request buildWeatherRequest(StCity state) throws Exception {
        String foreAddr = getString(R.string.forecast_addr);
        String urlStr = foreAddr + getString(R.string.lat_condition) + state.getStrLat() +
                getString(R.string.lon_condition) + state.getStrLon() +
                getString(R.string.add_condition);

        HttpUrl url = HttpUrl.parse(urlStr);
        if (url == null) {
            throw new MalformedURLException(getString(R.string.err_text));
        }

        return new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().maxStale(3, TimeUnit.SECONDS).build())
                .build();
    }

    private class WeatherCallback implements Callback {
        private final StCity state;
        private final StCityAdapter adapter;

        WeatherCallback(StCity state, StCityAdapter adapter) {
            this.state = state;
            this.adapter = adapter;
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            try {
                if (!response.isSuccessful()) {
                    handleFailedResponse();
                    return;
                }

                String responseData = response.body() != null ? response.body().string() : null;
                if (responseData == null) {
                    updateCityWithError(state, adapter, getString(R.string.err_text));
                    return;
                }

                processSuccessfulResponse(responseData);
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.err_text), e);
                updateCityWithError(state, adapter, getString(R.string.err_text));
            }
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e(TAG, getString(R.string.err_connect), e);
            updateCityWithError(state, adapter, getString(R.string.err_connect));
        }

        private void handleFailedResponse() {
            updateCityWithError(state, adapter, getString(R.string.err_text));
        }

        private void processSuccessfulResponse(String responseData) throws JSONException {
            JSONObject jo = new JSONObject(responseData);
            String tempFromAPI = jo.getJSONObject(getString(R.string.cur_weather))
                    .getString(getString(R.string.temperature));
            updateCityWithTemperature(state, adapter, tempFromAPI);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCityWithError(StCity city, StCityAdapter adapter, String error) {
        runOnUiThread(() -> {
            try {
                city.setTemp(error);
                city.setSyncDate(LocalDateTime.now());
                stcConnector.update(city);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.err_text), e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCityWithTemperature(StCity city, StCityAdapter adapter, String temperature) {
        runOnUiThread(() -> {
            try {
                city.setTemp(temperature);
                city.setSyncDate(LocalDateTime.now());
                stcConnector.update(city);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.err_text), e);
                updateCityWithError(city, adapter, getString(R.string.err_text));
            }
        });
    }

    private void refreshAllCities() {
        try {
            ArrayList<StCity> cityList = safeGetCities();
            if (cityList.isEmpty()) {
                showToast(getString(R.string.no_cities_saved));
                return;
            }

            pendingUpdates = cityList.size();
            showToast(getString(R.string.updating_data));

            for (StCity city : cityList) {
                if (city != null) {
                    refreshSingleCity(city);
                } else {
                    decrementCounterAndCheck();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            showToast(getString(R.string.err_text));
        }
    }

    private void refreshSingleCity(StCity cityData) {
        try {
            HttpUrl url = buildWeatherUrl(cityData);
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                    .build();

            new OkHttpClient().newCall(request).enqueue(new RefreshCallback(cityData));
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
            handleCityUpdate(cityData, getString(R.string.err_connect));
        }
    }

    private HttpUrl buildWeatherUrl(StCity city) {
        String apiUrl = getString(R.string.forecast_addr);
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(apiUrl)).newBuilder();
        builder.addQueryParameter("latitude", city.getStrLat());
        builder.addQueryParameter("longitude", city.getStrLon());
        builder.addQueryParameter("current_weather", "true");
        return builder.build();
    }

    private class RefreshCallback implements Callback {
        private final StCity cityData;

        RefreshCallback(StCity cityData) {
            this.cityData = cityData;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e(TAG, getString(R.string.err_connect), e);
            handleCityUpdate(cityData, getString(R.string.err_connect));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            try {
                if (response.code() != 200) {
                    handleCityUpdate(cityData, getString(R.string.err_text));
                    return;
                }

                String jsonResponse = response.body() != null ? response.body().string() : null;
                if (jsonResponse == null) {
                    handleCityUpdate(cityData, getString(R.string.err_text));
                    return;
                }

                processWeatherResponse(jsonResponse);
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.err_text), e);
                handleCityUpdate(cityData, getString(R.string.err_text));
            }
        }

        private void processWeatherResponse(String jsonResponse) throws JSONException {
            JSONObject jsonData = new JSONObject(jsonResponse);
            String temperature = jsonData.getJSONObject(getString(R.string.cur_weather))
                    .getString(getString(R.string.temperature));
            handleCityUpdate(cityData, temperature);
        }
    }

    private void handleCityUpdate(StCity city, String temperature) {
        runOnUiThread(() -> {
            try {
                city.setTemp(temperature);
                city.setSyncDate(LocalDateTime.now());
                stcConnector.update(city);
                decrementCounterAndCheck();
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.err_text), e);
                decrementCounterAndCheck();
            }
        });
    }

    private synchronized void decrementCounterAndCheck() {
        try {
            pendingUpdates--;
            if (pendingUpdates <= 0) {
                updateList();
                showToast(getString(R.string.update_complete));
            }
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.err_text), e);
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void handleCriticalError(String message, Exception e) {
        Log.e(TAG, message, e);
        showToast(message);
        finish();
    }
}