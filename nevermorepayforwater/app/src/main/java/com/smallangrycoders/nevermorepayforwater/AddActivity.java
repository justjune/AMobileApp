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
                                        "Could not get coordinates: " + error,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } else {
                    Toast.makeText(AddActivity.this,
                            "Please enter a location name first",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StCity stcity=new StCity(-1,etLoc.getText().toString(),"0", etLat.getText().toString(), etLon.getText().toString(), 1, LocalDateTime.now());
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
