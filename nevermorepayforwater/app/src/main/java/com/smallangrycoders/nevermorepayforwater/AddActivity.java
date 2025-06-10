package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.time.LocalDateTime;

public class AddActivity extends Activity {
    private Button btSave, btCancel, btSearch;
    private EditText etLoc, etLat, etLon;
    private ProgressBar progressBar;
    private GeocodingService geocodingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        
        geocodingService = new GeocodingService(this);
        
        btSave = findViewById(R.id.butSave);
        btCancel = findViewById(R.id.butCancel);
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
        String cityName = etLoc.getText().toString().trim();
        if (cityName.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_city_name), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btSearch.setEnabled(false);
        btSave.setEnabled(false);

        geocodingService.getCoordinates(cityName, new GeocodingService.GeocodingCallback() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                etLat.setText(String.valueOf(latitude));
                etLon.setText(String.valueOf(longitude));
                progressBar.setVisibility(View.GONE);
                btSearch.setEnabled(true);
                btSave.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btSearch.setEnabled(true);
                btSave.setEnabled(true);
            }
        });
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
