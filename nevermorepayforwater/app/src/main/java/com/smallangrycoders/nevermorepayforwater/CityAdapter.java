package com.smallangrycoders.nevermorepayforwater;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityItemViewHolder> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final OnCityClickListener onClickListener;
    private final List<City> cities;
    private final Context context;

    public CityAdapter(Context context, List<City> cities, OnCityClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.cities = cities;
        this.context = context;
    }

    @Override
    public CityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new CityItemViewHolder(view);
    }

    public void setCities(List<City> cities) {
        this.cities.clear();
        this.cities.addAll(cities);
    }

    @Override
    public void onBindViewHolder(CityItemViewHolder holder, int position) {
        position = holder.getAdapterPosition();

        City city = cities.get(position);
        String temperature = this.context.getString(R.string.tempro);

        holder.textViewCityName.setText(city.getName());
        holder.textViewTemperature.setText(temperature + city.getTemp() + "℃");

        if (city.getDateTime() != null) {
            String refreshed = this.context.getString(R.string.refreshed);
            holder.textViewSyncDate.setText(refreshed + " " + city.getDateTime().format(dateTimeFormatter));
        }

        int finalPosition = position;

        holder.itemView.setOnClickListener(v -> {
            onClickListener.onCityClick(city, finalPosition);
            if (city.getDateTime() != null) {
                holder.textViewTemperature.setText(R.string.refresh_data);
                holder.textViewSyncDate.setText(R.string.refresh_data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class CityItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewCityName;
        private final TextView textViewTemperature;
        private final TextView textViewSyncDate;

        CityItemViewHolder(View view) {
            super(view);
            textViewCityName = view.findViewById(R.id.textViewCityName);
            textViewTemperature = view.findViewById(R.id.textViewTemperature);
            textViewSyncDate = view.findViewById(R.id.textViewSyncDate);
        }
    }
}
