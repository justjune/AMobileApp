package com.smallangrycoders.nevermorepayforwater;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StCityAdapter  extends RecyclerView.Adapter<StCityAdapter.ViewHolder>{


    interface OnStCityClickListener{
        void onStCityClick(StCity state, int position);
    }

    private OnStCityClickListener onClickListener;
    DateTimeFormatter formatq = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final LayoutInflater inflater;
    private List<StCity> states;
    private final Context context;

    StCityAdapter(Context context, List<StCity> states, OnStCityClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.states = states;
        this.inflater = LayoutInflater.from(context);

        this.context = context;
    }

    public void SetOnCl(OnStCityClickListener onClickListener){ this.onClickListener = onClickListener;}
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }
    public boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDate.now());
    }
    public void setArrayMyData(List<StCity> arrayMyData) {
        this.states = arrayMyData;
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        StCity state = states.get(position);
        holder.nameView.setText(state.getName());
        String tempro = this.context.getString(R.string.tempro);
        holder.tempView.setText(tempro+ " " + state.getTemp());
        if (state.getSyncDate()!= null){
            String refreshed = this.context.getString(R.string.refreshed);
            holder.syncDateView.setText(refreshed+state.getSyncDate().format(formatq));
        }

        int finalPosition = position;
        View.OnClickListener listener = v -> {
            onClickListener.onStCityClick(state, finalPosition);
            if (state.getSyncDate()!= null){
                holder.tempView.setText(R.string.refresh_data);
                holder.syncDateView.setText(R.string.refresh_data);
            }
            double s = 0;
            for (String t : state.getPrevTemp()){
                s+= Math.abs(Double.parseDouble(t));
            }
            double water = 0;
            if (state.getWaterCons() != null) water = Double.parseDouble(state.getWaterCons());
            holder.txtGkal.setText(String.format("%.3f", s*water) + "Gkal");

        };
        holder.itemView.setOnClickListener(listener);
        holder.btnTemp.setOnClickListener(v -> {
            if (!isToday(state.getSyncDate())) {
                showNumberInputDialog(state, finalPosition);
                state.setSyncDate(LocalDateTime.now());
                double s = 0;
                for (String t : state.getPrevTemp()){
                    s+= Math.abs(Double.parseDouble(t));
                }
                double water = 0;
                if (state.getWaterCons() != null) water = Double.parseDouble(state.getWaterCons());
                holder.txtGkal.setText(String.format("%.3f", s*water) + "Gkal");
            } else {
                Toast.makeText(context, "Сегодня уже записана температура", Toast.LENGTH_SHORT).show();
            }
            onClickListener.onStCityClick(state, finalPosition);
            if (state.getSyncDate()!= null){
                holder.tempView.setText(R.string.refresh_data);
                holder.syncDateView.setText(R.string.refresh_data);
            }

        });
        holder.btnWaterCons.setOnClickListener(v -> {
            showWaterConsInputDialog(state, finalPosition);
            onClickListener.onStCityClick(state, finalPosition);
            if (state.getSyncDate()!= null){
                holder.tempView.setText(R.string.refresh_data);
                holder.syncDateView.setText(R.string.refresh_data);
            }
        });
        double s = 0;
        for (String t : state.getPrevTemp()){
            s+= Double.parseDouble(t);
        }


    }

    private void showWaterConsInputDialog(StCity state, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Введите расход воды в литрах");

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Изменить", (dialog, which) -> {
            String number = input.getText().toString();
            try {Double.valueOf(number);}
            catch (Exception e){
                return;
            }
            if (!number.isEmpty() && number.charAt(0)!='-') {
                state.setWaterCons(number);

                // Обновляем базу данных
                DBCities db = new DBCities(context);
                db.update(state);

                Toast.makeText(context, "Расход изменён", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Отмена", null);
        builder.show();

    };

    private void showNumberInputDialog(StCity state, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Сегодняшняя температура");

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String number = input.getText().toString();
            try {Double.valueOf(number);}
            catch (Exception e){
                return;
            }
            if (!number.isEmpty() && number.charAt(0)!='-') {
                state.addNumber(String.valueOf(Double.parseDouble(state.getTemp()) -Double.parseDouble(number)));

                DBCities db = new DBCities(context);
                db.update(state);

                Toast.makeText(context, "Число добавлено", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();

    };

    @Override
    public int getItemCount() {
        return states.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView nameView, tempView, syncDateView, txtGkal;
        final Button btnTemp, btnWaterCons;
        ViewHolder(View view){
            super(view);
            nameView = view.findViewById(R.id.name);
            tempView = view.findViewById(R.id.temp);
            syncDateView = view.findViewById(R.id.syncDate);
            btnTemp = view.findViewById(R.id.btnTemp);
            btnWaterCons = view.findViewById(R.id.btnWater);
            txtGkal = view.findViewById(R.id.txtGkal);
        }
    }
}
