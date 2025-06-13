package com.smallangrycoders.nevermorepayforwater;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class HeatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heat_activity);

        EditText etVol  = findViewById(R.id.etVolume);
        EditText etBat  = findViewById(R.id.etTempBattery);
        EditText etWat  = findViewById(R.id.etTempWater);
        Button   butCalc= findViewById(R.id.butCalcHeat);
        TextView tvRes  = findViewById(R.id.tvResult);
        Button   butExit= findViewById(R.id.butExit);

        butCalc.setOnClickListener(v -> {
            try {
                double V   = Double.parseDouble(etVol.getText().toString());
                double tB  = Double.parseDouble(etBat.getText().toString());
                double tW  = Double.parseDouble(etWat.getText().toString());
                double delta = tB - tW;

                if (delta <= 0) {
                    // Если вода теплее или равна батарее — смысла нет считать тепло
                    Toast.makeText(this,
                            "Темп. батареи должна быть выше темп. воды",
                            Toast.LENGTH_SHORT).show();
                    tvRes.setText("Ошибка: неверные данные");
                } else {
                    // 1 литр × 1°C ≈ 1 ккал
                    double kcal = delta * V;
                    tvRes.setText(String.format("Потрачено: %.2f ккал", kcal));
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this,
                        "Пожалуйста, введите все значения корректно",
                        Toast.LENGTH_SHORT).show();
            }
        });

        butExit.setOnClickListener(v -> finish());
    }
}
