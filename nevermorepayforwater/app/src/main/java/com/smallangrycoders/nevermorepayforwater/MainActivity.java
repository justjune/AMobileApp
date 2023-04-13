package com.smallangrycoders.nevermorepayforwater;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    ArrayList<StCity> states = new ArrayList<StCity>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // начальная инициализация списка
        setInitialData();
        RecyclerView recyclerView = findViewById(R.id.list);
        StCityAdapter adapter = new StCityAdapter(this, states, null);
        StCityAdapter.OnStCityClickListener stateClickListener = new StCityAdapter.OnStCityClickListener() {
            @Override
            public void onStCityClick(StCity state, int position) {

               sendPOST(state, adapter);
                state.setSyncDate(LocalDateTime.now());
                TextView retret = findViewById(R.id.textField);
                retret.setText(state.getSyncDate().toString());
            }
        };
       adapter.SetOnCl(stateClickListener);
       recyclerView.setAdapter(adapter);
    }

    private void setInitialData(){

        SQLiteDatabase db =  getBaseContext().openOrCreateDatabase("app8.db",MODE_PRIVATE , null);

        db.execSQL("CREATE TABLE IF NOT EXISTS cities (name TEXT, tempr TEXT, lat TEXT, lon TEXT, flag2 INTEGER, syncdate   TEXT, UNIQUE(name))");

        Cursor query = db.rawQuery("SELECT * FROM cities;", null);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        while(query.moveToNext()){
            String name = query.getString(0);
            int age = query.getInt(1);
            states.add(new StCity (query.getString(0), query.getString(1),query.getString(2), query.getString(3) ,query.getInt(4), LocalDateTime.parse(query.getString(5), format)));
        }
        query.close();
        db.close();
        if (states.isEmpty()) {
            states.add(new StCity("Бразилиа", " не обновлена", "-15.78", "-47.93", 1, null));
            states.add(new StCity("Буэнос-Айрес", "не обновлена", "-34.61", "-58.38", 1, null));
            states.add(new StCity("Богота", "не обновлена", "4.61", "-74.08", 1, null));
            states.add(new StCity("Монтевидео", "не обновлена", "-34.9", "-56.19", 1, null));
            states.add(new StCity("Сантьяго", "не обновлена", "-33.46", "-70.65", 1, null));
        }
        }
    public void sendPOST(StCity state, StCityAdapter adapter) {
        // создаем singleton объект клиента
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.open-meteo.com/v1/forecast?latitude="+state.getStrLat()+"&longitude="+state.getStrLon()+"&current_weather=true").newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().maxStale(30, TimeUnit.DAYS).build())
                .build();
          client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    final String responseData = response.body().string();
                    JSONObject jo = null;
                    try {
                        jo = new JSONObject(responseData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    String firstName;
                    try {
                        firstName =  jo.getJSONObject("current_weather").get("temperature").toString();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            state.setTemp(firstName);
                            adapter.notifyDataSetChanged();
                            SaveToDB();

                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void SaveToDB(){
        DateTimeFormatter formatq = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String whereCl = null;
        for (StCity b : states) {
            if (whereCl == null){
            whereCl = "('"+b.getName()+"', '"+b.getTemp()+"','"+b.getStrLat()+"', '"+b.getStrLon()+"', "+b.getFlagResource()+", '"+LocalDateTime.now().format(formatq)+"')"; }
            else {
                whereCl= whereCl+", "+ "('"+b.getName()+"', '"+b.getTemp()+"','"+b.getStrLat()+"', '"+b.getStrLon()+"', "+b.getFlagResource()+", '"+LocalDateTime.now().format(formatq)+"')";
            }
        }
        if (whereCl != null){
           SQLiteDatabase db =  getBaseContext().openOrCreateDatabase("app8.db",MODE_PRIVATE , null);
           String dbq = "INSERT OR REPLACE INTO cities VALUES "+whereCl+ ";";
           db.execSQL(dbq);
           db.close();
       }

    }
}