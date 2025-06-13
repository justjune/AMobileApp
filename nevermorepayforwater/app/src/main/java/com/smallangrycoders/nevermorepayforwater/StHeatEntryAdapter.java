package com.smallangrycoders.nevermorepayforwater;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class StHeatEntryAdapter extends RecyclerView.Adapter<StHeatEntryAdapter.ViewHolder> {
    private final List<StHeatEntry> entries = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public void setData(List<StHeatEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textTemps;

        ViewHolder(View view) {
            super(view);
            textDate = view.findViewById(R.id.textDate);
            textTemps = view.findViewById(R.id.textTemps);
        }
    }

    @NonNull
    @Override
    public StHeatEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heat_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StHeatEntryAdapter.ViewHolder holder, int position) {
        StHeatEntry e = entries.get(position);
        holder.textDate.setText(sdf.format(new Date(e.date)));
        holder.textTemps.setText(String.format(Locale.getDefault(),
                "Батарея: %.1f°C, Вода: %.1f°C", e.radiatorTemp, e.sourceTemp));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}
