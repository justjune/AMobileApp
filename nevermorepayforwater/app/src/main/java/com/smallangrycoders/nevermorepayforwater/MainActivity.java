package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_ACTIVITY  = 1;

    private DBCities      db;
    private Context       ctx;
    private StCityAdapter adapter;
    private StCity        selected;   // последний выбранный город

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;
        db  = new DBCities(this);

        RecyclerView rv = findViewById(R.id.list);
        adapter = new StCityAdapter(this, db.selectAll(), null, ctx);

        StCityAdapter.OnStCityClickListener click = (state, pos) -> {
            selected = state;
            sendPOST(state, adapter);
            state.setSyncDate(LocalDateTime.now());
        };
        adapter.SetOnCl(click);
        rv.setAdapter(adapter);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                startActivityForResult(new Intent(ctx, AddActivity.class), ADD_ACTIVITY);
                return true;

            case R.id.deleteAll:
                db.deleteAll();
                updateList();
                return true;

            case R.id.menu_enter_heat:
                openHeatActivity();
                return true;

            case R.id.menu_set_volume:
                showVolumeDialog();
                return true;

            case R.id.menu_heat_report:
                showHeatReportDialog();
                return true;

            case R.id.exit:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* ────────────────  Activity results  ──────────────── */
    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == Activity.RESULT_OK && data != null && req == ADD_ACTIVITY) {
            StCity st = (StCity) data.getSerializableExtra("StCity");
            if (st != null) {
                db.insert(st.getName(), st.getTemp(),
                        st.getStrLat(), st.getStrLon(),
                        st.getFlagResource(), st.getSyncDate());
                updateList();
            }
        }
    }

    private void openHeatActivity() {
        if (selected == null) {
            Toast.makeText(this, "Сначала выберите город", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, HeatActivity.class);
        i.putExtra("cityId", selected.getId());
        startActivity(i);
    }

    private void showVolumeDialog() {
        if (selected == null) {
            Toast.makeText(this, "Сначала выберите город", Toast.LENGTH_LONG).show();
            return;
        }

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("м³ за месяц");

        new AlertDialog.Builder(this)
                .setTitle("Объём воды")
                .setMessage("Введите объём воды из квитанции, м³/мес")
                .setView(input)
                .setPositiveButton("Сохранить", (d, w) -> {
                    try {
                        double vol = Double.parseDouble(input.getText().toString());
                        db.setCityVolume(selected.getId(), vol);
                        Toast.makeText(this, "Сохранено: " + vol + " м³", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Неверное число", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showHeatReportDialog() {
        if (selected == null) {
            Toast.makeText(this, "Сначала выберите город", Toast.LENGTH_LONG).show();
            return;
        }

        LocalDate today = LocalDate.now();
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (DatePicker dp, int y, int m, int d) -> {
                    YearMonth ym = YearMonth.of(y, m + 1);
                    double kcal;
                    try {
                        kcal = db.calcHeat(selected.getId(), ym);
                    } catch (Exception ex) {
                        Toast.makeText(this,
                                "Ошибка расчёта: " + ex.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Отчёт: " + ym)
                            .setMessage(String.format(
                                    "Тепло: %.0f ккал (%.3f Гкал)",
                                    kcal, kcal / 1_000_000))
                            .setPositiveButton("OK", null)
                            .show();
                },
                today.getYear(), today.getMonthValue() - 1, 1);

        int dayId = getResources().getIdentifier("day", "id", "android");
        if (dayId != 0) {
            android.view.View dayView = dlg.getDatePicker().findViewById(dayId);
            if (dayView != null) dayView.setVisibility(android.view.View.GONE);
        }

        dlg.setTitle("Выберите месяц");
        dlg.show();
    }

    private void updateList() {
        adapter.setArrayMyData(db.selectAll());
        adapter.notifyDataSetChanged();
    }

    public void sendPOST(StCity state, StCityAdapter adapter) {

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build();

        String url = HttpUrl.parse(
                        getString(R.string.forecast_addr)
                                + getString(R.string.lat_condition) + state.getStrLat()
                                + getString(R.string.lon_condition) + state.getStrLon()
                                + getString(R.string.add_condition))
                .newBuilder().build().toString();

        Request request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder()
                        .maxStale(3, TimeUnit.SECONDS).build())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ctx, R.string.err_network, Toast.LENGTH_LONG).show();
                    state.setTemp(getString(R.string.err_text));
                    adapter.notifyDataSetChanged();
                    db.update(state);
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ctx,
                                getString(R.string.err_server, response.code()),
                                Toast.LENGTH_LONG).show();
                        state.setTemp(getString(R.string.err_text));
                        adapter.notifyDataSetChanged();
                        db.update(state);
                    });
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject jo = new JSONObject(body);
                    String temp = jo.getJSONObject(getString(R.string.cur_weather))
                            .getString(getString(R.string.temperature));

                    runOnUiThread(() -> {
                        state.setTemp(temp);
                        adapter.notifyDataSetChanged();
                        db.update(state);
                    });

                } catch (JSONException ex) {
                    runOnUiThread(() -> Toast.makeText(ctx,
                            R.string.err_parse, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
