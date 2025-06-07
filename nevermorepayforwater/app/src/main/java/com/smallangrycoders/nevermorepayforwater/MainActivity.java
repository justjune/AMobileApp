package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
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
    ArrayList<StCity> states = new ArrayList<>();
    StCityAdapter adapter;
    int ADD_ACTIVITY = 0;

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
                Intent i = new Intent(oContext, AddActivity.class);
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
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
            try {
                StCity st = (StCity) data.getExtras().getSerializable("StCity");
                if (st != null) {
                    stcConnector.insert(st.getName(), st.getTemp(), st.getStrLat(), st.getStrLon(),
                            st.getFlagResource(), st.getSyncDate());
                    updateList();
                } else {
                    Toast.makeText(this, R.string.error_invalid_data, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_processing_data, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void sendPOST(StCity state, StCityAdapter adapter) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            runOnUiThread(() -> {
                state.setTemp(getString(R.string.err_no_internet));
                adapter.notifyDataSetChanged();
                stcConnector.update(state);
                Toast.makeText(oContext, R.string.err_no_internet, Toast.LENGTH_SHORT).show();
            });
            return;
        }

        OkHttpClient client = new OkHttpClient();
        try {
            String foreAddr = oContext.getString(R.string.forecast_addr);
            HttpUrl.Builder urlBuilder = HttpUrl.parse(foreAddr + oContext.getString(R.string.lat_condition) +
                    state.getStrLat() + oContext.getString(R.string.lon_condition) +
                    state.getStrLon() + oContext.getString(R.string.add_condition)).newBuilder();

            if (urlBuilder == null) {
                throw new MalformedURLException("Invalid URL");
            }

            String url = urlBuilder.build().toString();
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(new CacheControl.Builder().maxStale(3, TimeUnit.SECONDS).build())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        handleError(state, adapter, getString(R.string.err_server_response));
                        return;
                    }

                    try {
                        final String responseData = response.body().string();
                        JSONObject jo = new JSONObject(responseData);

                        if (!jo.has(oContext.getString(R.string.cur_weather))) {
                            handleError(state, adapter, getString(R.string.err_invalid_response));
                            return;
                        }

                        String tempFromAPI = jo.getJSONObject(oContext.getString(R.string.cur_weather))
                                .getString(oContext.getString(R.string.temperature));

                        runOnUiThread(() -> {
                            state.setTemp(tempFromAPI);
                            state.setSyncDate(LocalDateTime.now());
                            adapter.notifyDataSetChanged();
                            stcConnector.update(state);
                        });
                    } catch (JSONException e) {
                        handleError(state, adapter, getString(R.string.err_parsing_data));
                    } catch (Exception e) {
                        handleError(state, adapter, getString(R.string.err_unknown));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    handleError(state, adapter, getString(R.string.err_connection_failed));
                }
            });
        } catch (MalformedURLException e) {
            handleError(state, adapter, getString(R.string.err_invalid_url));
        } catch (Exception e) {
            handleError(state, adapter, getString(R.string.err_unknown));
        }
    }

    private void handleError(StCity state, StCityAdapter adapter, String error) {
        runOnUiThread(() -> {
            state.setTemp(error);
            adapter.notifyDataSetChanged();
            stcConnector.update(state);
            Toast.makeText(oContext, error, Toast.LENGTH_SHORT).show();
        });
    }
}