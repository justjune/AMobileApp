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
    private EditText etTempIn, etTempOut, etVolume;
    private Button btnSave, btnShowStats;
    private TextView tvResult;
    private DBCities db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);

        db = new DBCities(this);

        etTempIn = findViewById(R.id.etTempIn);
        etTempOut = findViewById(R.id.etTempOut);
        etVolume = findViewById(R.id.etVolume);
        btnSave = findViewById(R.id.btnSaveHeatData);
        btnShowStats = findViewById(R.id.btnShowStats);
        tvResult = findViewById(R.id.tvHeatResult);

        btnSave.setOnClickListener(v -> saveHeatData());
        btnShowStats.setOnClickListener(v -> showStatistics());
    }

    private void saveHeatData() {
        try {
            double tempIn = Double.parseDouble(etTempIn.getText().toString());
            double tempOut = Double.parseDouble(etTempOut.getText().toString());
            double volume = Double.parseDouble(etVolume.getText().toString());

            db.insertHeatData(LocalDateTime.now(), tempIn, tempOut, volume);

            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            calculateHeat(tempIn, tempOut, volume);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте введенные данные", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateHeat(double tempIn, double tempOut, double volume) {
        double heat = volume * 1000 * (tempIn - tempOut); // Переводим м³ в литры
        String result = String.format("Расход тепла: %.2f ккал", heat);
        tvResult.setText(result);
    }

    private void showStatistics() {
        List<HeatRecord> records = db.getAllHeatData();
        if (records.isEmpty()) {
            tvResult.setText("Нет данных для отображения");
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
        tvResult.setText(stats.toString());
    }
}