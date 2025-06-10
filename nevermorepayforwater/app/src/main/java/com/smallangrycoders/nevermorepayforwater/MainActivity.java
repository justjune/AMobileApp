package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    DBCities stcConnector;
    Context oContext;
    ArrayList<StCity> states = new ArrayList<StCity>();
    StCityAdapter adapter;
    int ADD_ACTIVITY = 0;
    private int pendingUpdates = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.list);
        oContext=this;
        stcConnector=new DBCities(this);
        adapter = new StCityAdapter(this, stcConnector.selectAll(), null, oContext);
        StCityAdapter.OnStCityClickListener stateClickListener = (state, position) -> {

           sendPOST(state, adapter);
            state.setSyncDate(LocalDateTime.now());

        };
       adapter.SetOnCl(stateClickListener);
       recyclerView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void updateList () {
        adapter.setArrayMyData(stcConnector.selectAll());
        adapter.notifyDataSetChanged();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            Intent intent = new Intent(this, AddActivity.class);
            startActivityForResult(intent, ADD_ACTIVITY);
            return true;
        } else if (id == R.id.deleteAll) {
            stcConnector.deleteAll();
            updateList();
            return true;
        } else if (id == R.id.exit) {
            finish();
            return true;
        } else if (id == R.id.refresh_all) {
            refreshAllCities();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            StCity st = (StCity) data.getExtras().getSerializable("StCity");
            stcConnector.insert(st.getName(), st.getTemp(), st.getStrLat(), st.getStrLon(), st.getFlagResource(), st.getSyncDate());
            updateList();

        }
    }
    public void sendPOST(StCity state, StCityAdapter adapter) {
        OkHttpClient client = new OkHttpClient();
        String foreAddr = oContext.getString(R.string.forecast_addr);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(foreAddr+oContext.getString(R.string.lat_condition)+state.getStrLat()+oContext.getString(R.string.lon_condition)+state.getStrLon()+oContext.getString(R.string.add_condition)).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().maxStale(3, TimeUnit.SECONDS).build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful())
                    {
                    MainActivity.this.runOnUiThread(() -> {
                        state.setTemp(oContext.getString(R.string.err_text));
                        adapter.notifyDataSetChanged();
                        stcConnector.update(state);
                    });
                    }
                else
                    {
                    final String responseData = response.body().string();
                    JSONObject jo;
                    try {
                        jo = new JSONObject(responseData);
                        }
                    catch (JSONException e)
                        {
                            throw new RuntimeException(e);
                        }
                    String tempFromAPI;
                    try {
                        tempFromAPI =  jo.getJSONObject(oContext.getString(R.string.cur_weather)).get(oContext.getString(R.string.temperature)).toString();
                        }
                    catch (JSONException e)
                        {
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
    }

    private void refreshAllCities() {
        ArrayList<StCity> cities = stcConnector.selectAll();
        if (cities.isEmpty()) {
            Toast.makeText(this, "Нет сохраненных городов", Toast.LENGTH_SHORT).show();
            return;
        }
        
        pendingUpdates = cities.size();
        Toast.makeText(this, "Обновляем данные...", Toast.LENGTH_SHORT).show();
        
        for (StCity city : cities) {
            refreshCity(city);
        }
    }

    private void refreshCity(StCity city) {
        String addr = getString(R.string.forecast_addr);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(addr).newBuilder();
        urlBuilder.addQueryParameter("latitude", city.getStrLat());
        urlBuilder.addQueryParameter("longitude", city.getStrLon());
        urlBuilder.addQueryParameter("current_weather", "true");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    city.setTemp(getString(R.string.err_connect));
                    city.setSyncDate(LocalDateTime.now());
                    stcConnector.update(city);
                    checkAndUpdateUI();
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        city.setTemp(getString(R.string.err_text));
                        city.setSyncDate(LocalDateTime.now());
                        stcConnector.update(city);
                        checkAndUpdateUI();
                    });
                    return;
                }

                try {
                    final String responseData = response.body().string();
                    JSONObject jo = new JSONObject(responseData);
                    String tempFromAPI = jo.getJSONObject(getString(R.string.cur_weather))
                            .get(getString(R.string.temperature)).toString();

                    runOnUiThread(() -> {
                        city.setTemp(tempFromAPI);
                        city.setSyncDate(LocalDateTime.now());
                        stcConnector.update(city);
                        checkAndUpdateUI();
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        city.setTemp(getString(R.string.err_text));
                        city.setSyncDate(LocalDateTime.now());
                        stcConnector.update(city);
                        checkAndUpdateUI();
                    });
                }
            }
        });
    }

    private synchronized void checkAndUpdateUI() {
        pendingUpdates--;
        if (pendingUpdates <= 0) {
            updateList();
            Toast.makeText(this, "Обновление завершено", Toast.LENGTH_SHORT).show();
        }
    }
}