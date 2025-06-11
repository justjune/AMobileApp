package com.smallangrycoders.nevermorepayforwater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StCityAdapter extends RecyclerView.Adapter<StCityAdapter.ViewHolder> {
    interface OnStCityClickListener {
        void onStCityClick(StCity state, int position);
    }

    private OnStCityClickListener onClickListener;
    DateTimeFormatter formatq = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final LayoutInflater inflater;
    private List<StCity> states;
    private final Context context;

    StCityAdapter(Context context, List<StCity> states, OnStCityClickListener onClickListener, Context context1) {
        this.onClickListener = onClickListener;
        this.states = states;
        this.inflater = LayoutInflater.from(context);
        this.context = context1;
    }

    public void SetOnCl(OnStCityClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    public void setArrayMyData(List<StCity> arrayMyData) {
        this.states = arrayMyData;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        StCity state = states.get(position);
        
        // Set city name
        holder.nameView.setText(state.getName());
        
        // Format temperature with degree symbol
        try {
            float temp = Float.parseFloat(state.getTemp());
            holder.tempView.setText(String.format("%.1f°C", temp));
        } catch (NumberFormatException e) {
            holder.tempView.setText(state.getTemp());
        }

        // Set sync date
        if (state.getSyncDate() != null) {
            String refreshed = this.context.getString(R.string.refreshed);
            holder.syncDateView.setText(refreshed + state.getSyncDate().format(formatq));
        }

        int finalPosition = position;
        holder.itemView.setOnClickListener(v -> {
            onClickListener.onStCityClick(state, finalPosition);
            if (state.getSyncDate() != null) {
                holder.tempView.setText(R.string.refresh_data);
                holder.syncDateView.setText(R.string.refresh_data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return states.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView, tempView, syncDateView;
        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.name);
            tempView = view.findViewById(R.id.temp);
            syncDateView = view.findViewById(R.id.syncDate);
        }
    }
}
