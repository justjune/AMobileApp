package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class AddActivity extends Activity {
    private Button btSave, btCancel, btFindCoordinates;
    private EditText etLoc, etLat, etLon;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        btSave=(Button)findViewById(R.id.butSave);
        btCancel=(Button)findViewById(R.id.butCancel);
        etLoc=(EditText)findViewById(R.id.City);
        etLat=(EditText)findViewById(R.id.etLat);
        etLon=(EditText)findViewById(R.id.etLon);
        btFindCoordinates=(Button)findViewById(R.id.butFindCoordinates);


        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String loc = etLoc.getText().toString();
                    String lat = etLat.getText().toString();
                    String lon = etLon.getText().toString();

                    if (loc.isEmpty()) {
                        Toast.makeText(AddActivity.this, "Введите название населенного пункта", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (lat.isEmpty() || lon.isEmpty()) {
                        Toast.makeText(AddActivity.this, "Введите координаты", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StCity stcity = new StCity(-1, loc, "0", lat, lon, 1, LocalDateTime.now());
                    Intent intent = getIntent();
                    intent.putExtra("StCity", stcity);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(AddActivity.this, "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btFindCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = etLoc.getText().toString();
                if (locationName.isEmpty()) {
                    Toast.makeText(AddActivity.this, "Введите название населенного пункта", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(AddActivity.this);
                progressDialog.setMessage("Поиск координат...");
                progressDialog.show();

                findCoordinates(locationName);
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findCoordinates(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etLat.setText(String.valueOf(address.getLatitude()));
                etLon.setText(String.valueOf(address.getLongitude()));
            } else {
                Toast.makeText(this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка геокодирования: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}
