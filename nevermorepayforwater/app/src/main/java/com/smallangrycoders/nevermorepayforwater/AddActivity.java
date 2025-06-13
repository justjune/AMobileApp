package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.time.LocalDateTime;

public class AddActivity extends Activity {

    public static final String CITY_CODE = "ADD_ACTIVITY_CITY";
    public static final String CITY_NAME_CODE = "ADD_ACTIVITY_CITY_NAME";
    public static final String LATITUDE_CODE = "ADD_ACTIVITY_LATITUDE";
    public static final String LONGITUDE_CODE = "ADD_ACTIVITY_LONGITUDE";

    private Button buttonSave;
    private Button buttonCancel;

    private EditText editTextCityName;
    private EditText editTextLatitude;
    private EditText editTextLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        initEditTexts();
        initButtons();
    }

    private void initButtons() {
        buttonSave = findViewById(R.id.butSave);
        buttonCancel = findViewById(R.id.butCancel);

        buttonSave.setOnClickListener(v -> {
            City city = new City(-1, editTextCityName.getText().toString(), "0", editTextLatitude.getText().toString(), editTextLongitude.getText().toString(), 1, LocalDateTime.now());
            Intent intent = getIntent();
            intent.putExtra(CITY_CODE, city);
            setResult(RESULT_OK, intent);
            finish();
        });

        buttonCancel.setOnClickListener(v -> {
            finish();
        });
    }

    private void initEditTexts() {
        editTextCityName = findViewById(R.id.editTextCityName);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String cityName = extras.getString(CITY_NAME_CODE);
            String latitude = extras.getString(LATITUDE_CODE);
            String longitude = extras.getString(LONGITUDE_CODE);

            editTextCityName.setText(cityName);
            editTextLatitude.setText(latitude);
            editTextLongitude.setText(longitude);
        }
    }
}
