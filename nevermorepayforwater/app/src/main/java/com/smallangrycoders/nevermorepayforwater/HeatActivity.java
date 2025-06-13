package com.smallangrycoders.nevermorepayforwater;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HeatActivity extends AppCompatActivity {
    private EditText editTextTempIn;
    private EditText editTextTempOut;
    private EditText editTextVolume;

    private Button buttonSave;
    private Button buttonShowStats;

    private TextView textViewResult;
    private DBCities dbCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);

        dbCities = new DBCities(this);

        initEditTexts();
        initButtons();
    }

    private void initEditTexts() {
        editTextTempIn = findViewById(R.id.editTextTempIn);
        editTextTempOut = findViewById(R.id.editTextTempOut);
        editTextVolume = findViewById(R.id.etVolume);
    }

    private void initButtons() {
        buttonSave = findViewById(R.id.btnSaveHeatData);
        buttonShowStats = findViewById(R.id.btnShowStats);
        textViewResult = findViewById(R.id.tvHeatResult);

        buttonSave.setOnClickListener(v -> {
            saveHeatData();
        });
        buttonShowStats.setOnClickListener(v -> {
            showStatistics();
        });
    }

    private void saveHeatData() {
        try {
            double tempIn = Double.parseDouble(editTextTempIn.getText().toString());
            double tempOut = Double.parseDouble(editTextTempOut.getText().toString());
            double volume = Double.parseDouble(editTextVolume.getText().toString());

            dbCities.insertHeatData(LocalDateTime.now(), tempIn, tempOut, volume);

            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            calculateHeat(tempIn, tempOut, volume);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте введенные данные", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateHeat(double tempIn, double tempOut, double volume) {
        double heat = volume * 1000 * (tempIn - tempOut);
        String result = String.format("Расход тепла: %.2f ккал", heat);
        textViewResult.setText(result);
    }

    private void showStatistics() {
        List<HeatRecord> records = dbCities.getAllHeatData();
        if (records.isEmpty()) {
            textViewResult.setText("Нет данных");
            return;
        }

        StringBuilder stats = new StringBuilder("Статистика:\n");
        double totalHeat = 0;

        for (HeatRecord record : records) {
            double dailyHeat = record.getVolume() * 1000 *
                    (record.getTempIn() - record.getTempOut());
            totalHeat += dailyHeat;

            stats.append(String.format("%s: %.2f ккал\n",
                    record.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    dailyHeat));
        }

        stats.append(String.format("\nИтого: %.2f ккал", totalHeat));
        textViewResult.setText(stats.toString());
    }
}