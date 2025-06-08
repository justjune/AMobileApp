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
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddActivity extends Activity {
    private Button btSave, btCancel, btSearch;
    private EditText etLoc, etLat, etLon;
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-',.]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        btSave = findViewById(R.id.butSave);
        btCancel = findViewById(R.id.butCancel);
        btSearch = findViewById(R.id.butSearch);
        etLoc = findViewById(R.id.City);
        etLat = findViewById(R.id.etLat);
        etLon = findViewById(R.id.etLon);

        btSearch.setOnClickListener(v -> {
            String name = etLoc.getText().toString().trim();
            if (name.isEmpty()) {
                etLoc.setError(getString(R.string.error_empty_name));
                return;
            }
            if (!ENGLISH_PATTERN.matcher(name).matches()) {
                etLoc.setError(getString(R.string.error_english_only));
                return;
            }
            searchLocation(name);
        });

        btSave.setOnClickListener(v -> {
            String latStr = etLat.getText().toString();
            String lonStr = etLon.getText().toString();
            String name = etLoc.getText().toString().trim();

            if (name.isEmpty()) {
                etLoc.setError(getString(R.string.error_empty_name));
                return;
            }
            if (!ENGLISH_PATTERN.matcher(name).matches()) {
                etLoc.setError(getString(R.string.error_english_only));
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);

                if (lat < -90 || lat > 90) {
                    etLat.setError(getString(R.string.error_invalid_lat));
                    return;
                }

                if (lon < -180 || lon > 180) {
                    etLon.setError(getString(R.string.error_invalid_lon));
                    return;
                }

                StCity stcity = new StCity(-1, name, "0", latStr, lonStr, 1, LocalDateTime.now());
                Intent intent = getIntent();
                intent.putExtra("StCity", stcity);
                setResult(RESULT_OK, intent);
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_coordinates, Toast.LENGTH_SHORT).show();
            }
        });

        btCancel.setOnClickListener(v -> finish());
    }

    private void searchLocation(String name) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(getString(R.string.geocoding_addr))).newBuilder();
        urlBuilder.addQueryParameter("name", name);
        urlBuilder.addQueryParameter("count", "1");
        urlBuilder.addQueryParameter("language", "en");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddActivity.this, R.string.err_connection_failed, Toast.LENGTH_LONG).show();
                    Toast.makeText(AddActivity.this, R.string.try_examples, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddActivity.this, R.string.err_server_response, Toast.LENGTH_LONG).show();
                        Toast.makeText(AddActivity.this, R.string.try_examples, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                try {
                    String responseData = Objects.requireNonNull(response.body()).string();
                    JSONObject json = new JSONObject(responseData);
                    if (!json.has("results")) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddActivity.this, R.string.err_location_not_found, Toast.LENGTH_LONG).show();
                            Toast.makeText(AddActivity.this, R.string.try_examples, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    JSONArray results = json.getJSONArray("results");
                    if (results.length() == 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddActivity.this, R.string.err_location_not_found, Toast.LENGTH_LONG).show();
                            Toast.makeText(AddActivity.this, R.string.try_examples, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    JSONObject location = results.getJSONObject(0);
                    double latitude = location.getDouble("latitude");
                    double longitude = location.getDouble("longitude");

                    runOnUiThread(() -> {
                        etLat.setText(String.valueOf(latitude));
                        etLon.setText(String.valueOf(longitude));
                        Toast.makeText(AddActivity.this, R.string.location_found, Toast.LENGTH_SHORT).show();
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddActivity.this, R.string.err_parsing_data, Toast.LENGTH_LONG).show();
                        Toast.makeText(AddActivity.this, R.string.try_examples, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
