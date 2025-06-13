package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.LocalDateTime;
public class AddActivity extends Activity {
    private Button btSave,btCancel, btLookup;
    private EditText etLoc,etLat,etLon;

    private ApiService apiService = ApiService.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        btSave=(Button)findViewById(R.id.butSave);
        btCancel=(Button)findViewById(R.id.butCancel);
        btLookup=(Button)findViewById(R.id.butLookup);

        etLoc=(EditText)findViewById(R.id.City);
        etLat=(EditText)findViewById(R.id.etLat);
        etLon=(EditText)findViewById(R.id.etLon);

        btLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationName = etLoc.getText().toString();
                if (!locationName.isEmpty()) {
                    apiService.getCoordinates(locationName, new ApiService.GeocodingCallback() {
                        @Override
                        public void onSuccess(String lat, String lon) {
                            runOnUiThread(() -> {
                                etLat.setText(lat);
                                etLon.setText(lon);
                            });
                        }

                        @Override
                        public void onFailure(String error) {

                            runOnUiThread(() -> {
                                Toast.makeText(AddActivity.this,
                                        getString(R.string.noSuchLoc) + " " + error ,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } else {
                    Toast.makeText(AddActivity.this,
                            R.string.enterName,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = etLat.getText().toString();
                String lon = etLon.getText().toString();

                try {
                    double latValue = Double.parseDouble(lat);
                    double lonValue = Double.parseDouble(lon);
                    if (latValue < -90.0 || latValue > 90.0) {
                        Toast.makeText(AddActivity.this,
                                R.string.wrongCoord,
                                Toast.LENGTH_SHORT).show();
                        return;
                    };
                    if (lonValue < -180.0 || lonValue > 180.0) {
                        Toast.makeText(AddActivity.this,
                                R.string.wrongCoord,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Если строки не могут быть преобразованы в число
                    Toast.makeText(AddActivity.this,
                            R.string.wrongCoord,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                StCity stcity=new StCity(-1,etLoc.getText().toString(),"0", lat, lon, 1, LocalDateTime.now());
                Intent intent=getIntent();
                intent.putExtra("StCity", stcity);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
