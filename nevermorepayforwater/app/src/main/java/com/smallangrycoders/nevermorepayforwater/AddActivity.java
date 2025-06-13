package com.smallangrycoders.nevermorepayforwater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.time.LocalDateTime;

public class AddActivity extends Activity {
    private Button btSave;
    private Button btSearch;
    private EditText etLoc, etLat, etLon;
    private ProgressBar progressBar;
    private Geocoder geocoder;

    @SuppressLint("UnsafeIntentLaunch")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        geocoder = new Geocoder(this);

        btSave = findViewById(R.id.butSave);
        Button btCancel = findViewById(R.id.butCancel);
        btSearch = findViewById(R.id.butSearch);
        etLoc = findViewById(R.id.City);
        etLat = findViewById(R.id.etLat);
        etLon = findViewById(R.id.etLon);
        progressBar = findViewById(R.id.progressBar);

        btSearch.setOnClickListener(v -> searchLocation());

        btSave.setOnClickListener(v -> {
            if (validateFields()) {
                StCity stcity = new StCity(-1,
                        etLoc.getText().toString(),
                        "0",
                        etLat.getText().toString(),
                        etLon.getText().toString(),
                        1,
                        LocalDateTime.now()
                );
                Intent intent = getIntent();
                intent.putExtra("StCity", stcity);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btCancel.setOnClickListener(v -> finish());
    }

    private void searchLocation() {
        final String locationName = etLoc.getText().toString().trim();

        if (TextUtils.isEmpty(locationName)) {
            Toast.makeText(this, R.string.enter_city_name, Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        geocoder.fetchCoordinates(locationName, new Geocoder.ResultCallback() {
            @Override
            public void onLocationFound(double latitude, double longitude) {
                updateLocationFields(latitude, longitude);
                showProgress(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                showError(errorMessage);
                showProgress(false);
            }
        });
    }

    private void showProgress(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btSearch.setEnabled(!isLoading);
        btSave.setEnabled(!isLoading);
    }

    private void updateLocationFields(double latitude, double longitude) {
        etLat.setText(String.valueOf(latitude));
        etLon.setText(String.valueOf(longitude));
    }

    private void showError(String message) {
        Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateFields() {
        if (etLoc.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_city_name), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etLat.getText().toString().trim().isEmpty() || etLon.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.err_no_results), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
