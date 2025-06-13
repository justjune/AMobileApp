package com.smallangrycoders.nevermorepayforwater;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Date;
import java.util.List;

public class HeatEntryActivity extends AppCompatActivity {

    private EditText editRadiatorTemp, editSourceTemp;
    private Button btnSave, btnClear; // добавили кнопку очистить
    private RecyclerView recyclerView;
    private TextView textSummary;

    private StHeatEntryAdapter adapter;
    private DBHeatEntries db;

    private static final double DAILY_WATER_VOLUME_LITERS = 100.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_entry);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DBHeatEntries(this);
        editRadiatorTemp = findViewById(R.id.editRadiatorTemp);
        editSourceTemp = findViewById(R.id.editSourceTemp);
        btnSave = findViewById(R.id.btnSaveHeatEntry);
        btnClear = findViewById(R.id.btnClearHeatEntries); // привязываем кнопку очистить
        recyclerView = findViewById(R.id.recyclerHeatEntries);
        textSummary = findViewById(R.id.textSummary);

        adapter = new StHeatEntryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadData();

        btnSave.setOnClickListener(v -> {
            try {
                double radiator = Double.parseDouble(editRadiatorTemp.getText().toString());
                double source = Double.parseDouble(editSourceTemp.getText().toString());
                db.insert(new StHeatEntry(new Date(), radiator, source));
                editRadiatorTemp.setText("");
                editSourceTemp.setText("");
                loadData();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректные значения", Toast.LENGTH_SHORT).show();
            }
        });

        btnClear.setOnClickListener(v -> {
            db.clearAll();  // Метод, который удалит все записи из базы
            loadData();     // Обновим UI
            Toast.makeText(this, "Все записи удалены", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadData() {
        List<StHeatEntry> entries = db.getAll();
        adapter.setData(entries);

        if (!entries.isEmpty()) {
            double sumRadiator = 0, sumSource = 0;
            double totalTemperatureDifference = 0;

            for (StHeatEntry entry : entries) {
                sumRadiator += entry.radiatorTemp;
                sumSource += entry.sourceTemp;

                double diff = entry.radiatorTemp - entry.sourceTemp;
                if (diff > 0) {
                    totalTemperatureDifference += diff;
                }
            }

            double avgRadiator = sumRadiator / entries.size();
            double avgSource = sumSource / entries.size();
            double totalHeatCalories = totalTemperatureDifference * DAILY_WATER_VOLUME_LITERS;

            String summary = String.format(
                    "Средняя температура батареи: %.1f°C\n" +
                            "Средняя температура источника: %.1f°C\n" +
                            "Потребленное тепло за весь период: %.2f ккал",
                    avgRadiator, avgSource, totalHeatCalories
            );

            textSummary.setText(summary);
        } else {
            textSummary.setText("Нет записей");
        }
    }
}
