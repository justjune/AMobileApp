package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
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

    private ApiService apiService = ApiService.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.list);
        oContext=this;
        stcConnector=new DBCities(this);
        adapter = new StCityAdapter(this, stcConnector.selectAll(), null);
        StCityAdapter.OnStCityClickListener stateClickListener = (state, position) -> {

            sendPOST(state, adapter);
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
            stcConnector.insert(st.getName(), st.getTemp(), st.getStrLat(), st.getStrLon(), st.getFlagResource(), st.getSyncDate(), new ArrayList<String>());
            updateList();

        }
    }
    public void sendPOST(StCity state, StCityAdapter adapter) {

        apiService.getWeather(state, new ApiService.WeatherCallback() {
            @Override
            public void onSuccess(String temperature) {
                runOnUiThread(() -> {
                    state.setTemp(temperature);
                    adapter.notifyDataSetChanged();
                    stcConnector.update(state);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    state.setTemp(error);
                    adapter.notifyDataSetChanged();
                    stcConnector.update(state);
                });
            }
        });
    }

}