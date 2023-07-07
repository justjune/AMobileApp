package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        switch (item.getItemId()) {
            case R.id.add:
                Intent i = new Intent(oContext, AddActivity.class);
                startActivityForResult (i, ADD_ACTIVITY);
                return true;
            case R.id.deleteAll:
                stcConnector.deleteAll();
                updateList ();
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

}