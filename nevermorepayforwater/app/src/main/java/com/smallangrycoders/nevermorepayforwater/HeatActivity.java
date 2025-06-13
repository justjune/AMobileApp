package com.smallangrycoders.nevermorepayforwater;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;

public class HeatActivity extends AppCompatActivity {

    private EditText etBat;   // температура батареи
    private EditText etSrc;   // температура источника
    private DatePicker dp;    // выбор даты
    private long cityId;      // город, к которому относится запись

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);

        cityId = getIntent().getLongExtra("cityId", -1);
        if (cityId == -1) {    // подстраховка
            Toast.makeText(this, "Некорректный cityId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etBat = findViewById(R.id.etBat);
        etSrc = findViewById(R.id.etSrc);
        dp    = findViewById(R.id.datePicker);
        Button butSave = findViewById(R.id.butSaveHeat);

        butSave.setOnClickListener(v -> {
            String sBat = etBat.getText().toString().trim();
            String sSrc = etSrc.getText().toString().trim();

            if (sBat.isEmpty() || sSrc.isEmpty()) {
                Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_LONG).show();
                return;
            }

            double tBat, tSrc;
            try {
                tBat = Double.parseDouble(sBat);
                tSrc = Double.parseDouble(sSrc);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_LONG).show();
                return;
            }

            LocalDate date = LocalDate.of(
                    dp.getYear(), dp.getMonth() + 1, dp.getDayOfMonth());

            DBCities db = new DBCities(this);
            db.insertHeat(cityId, date, tBat, tSrc);

            Toast.makeText(this, R.string.msg_heat_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
