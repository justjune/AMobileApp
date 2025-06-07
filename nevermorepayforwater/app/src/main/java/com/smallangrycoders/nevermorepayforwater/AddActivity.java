package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.LocalDateTime;

public class AddActivity extends Activity {
    private Button btSave, btCancel;
    private EditText etLoc, etLat, etLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        btSave = (Button) findViewById(R.id.butSave);
        btCancel = (Button) findViewById(R.id.butCancel);
        etLoc = (EditText) findViewById(R.id.City);
        etLat = (EditText) findViewById(R.id.etLat);
        etLon = (EditText) findViewById(R.id.etLon);

        btSave.setOnClickListener(v -> {
            String latStr = etLat.getText().toString();
            String lonStr = etLon.getText().toString();
            String name = etLoc.getText().toString();

            if (name.isEmpty()) {
                etLoc.setError(getString(R.string.error_empty_name));
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
}
