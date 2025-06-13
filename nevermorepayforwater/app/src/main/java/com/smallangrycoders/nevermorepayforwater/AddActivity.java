package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddActivity extends Activity {

    private Button   btSave, btCancel, btGeo;
    private EditText etLoc, etLat, etLon;

    private final OkHttpClient geoClient = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        btSave   = findViewById(R.id.butSave);
        btCancel = findViewById(R.id.butCancel);
        btGeo    = findViewById(R.id.butGeo);
        etLoc    = findViewById(R.id.City);
        etLat    = findViewById(R.id.etLat);
        etLon    = findViewById(R.id.etLon);

        btGeo.setOnClickListener(v -> geocodeCity());

        btSave.setOnClickListener(v -> {
            String name = etLoc.getText().toString().trim();
            String lat  = etLat.getText().toString().trim();
            String lon  = etLon.getText().toString().trim();

            if (name.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
                Toast.makeText(this,
                        R.string.err_empty_fields,
                        Toast.LENGTH_LONG).show();
                return;
            }

            double dLat, dLon;
            try {
                dLat = Double.parseDouble(lat);
                dLon = Double.parseDouble(lon);
            } catch (NumberFormatException ex) {
                Toast.makeText(this,
                        R.string.err_not_number,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (Math.abs(dLat) > 90 || Math.abs(dLon) > 180) {
                Toast.makeText(this,
                        R.string.err_wrong_range,
                        Toast.LENGTH_LONG).show();
                return;
            }

            StCity stcity = new StCity(
                    -1, name, "0",
                    lat, lon,
                    1,
                    LocalDateTime.now()
            );

            Intent intent = new Intent();
            intent.putExtra("StCity", stcity);
            setResult(RESULT_OK, intent);

            Toast.makeText(this,
                    R.string.msg_city_saved,
                    Toast.LENGTH_SHORT).show();

            finish();
        });

        btCancel.setOnClickListener(v -> finish());
    }

    private void geocodeCity() {
        String query = etLoc.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this,
                    R.string.err_empty_fields,
                    Toast.LENGTH_LONG).show();
            return;
        }

        btGeo.setEnabled(false);

        HttpUrl url = HttpUrl.parse("https://nominatim.openstreetmap.org/search")
                .newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .build();

        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "NevermorePayForWater/1.0 (Android)")
                .build();

        geoClient.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddActivity.this,
                            R.string.err_network,
                            Toast.LENGTH_LONG).show();
                    btGeo.setEnabled(true);
                });
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                runOnUiThread(() -> btGeo.setEnabled(true));

                if (!resp.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(AddActivity.this,
                            getString(R.string.err_server, resp.code()),
                            Toast.LENGTH_LONG).show());
                    return;
                }

                String body = resp.body().string();
                try {
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() == 0) {
                        runOnUiThread(() -> Toast.makeText(AddActivity.this,
                                R.string.err_city_not_found,
                                Toast.LENGTH_LONG).show());
                        return;
                    }

                    JSONObject obj = arr.getJSONObject(0);
                    String lat = obj.getString("lat");
                    String lon = obj.getString("lon");

                    runOnUiThread(() -> {
                        etLat.setText(lat);
                        etLon.setText(lon);
                        Toast.makeText(AddActivity.this,
                                R.string.msg_coords_found,
                                Toast.LENGTH_SHORT).show();
                    });

                } catch (JSONException ex) {
                    runOnUiThread(() -> Toast.makeText(AddActivity.this,
                            R.string.err_parse,
                            Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
