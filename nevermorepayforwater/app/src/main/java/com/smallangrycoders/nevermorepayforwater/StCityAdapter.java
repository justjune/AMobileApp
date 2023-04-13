package com.smallangrycoders.nevermorepayforwater;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StCityAdapter  extends RecyclerView.Adapter<StCityAdapter.ViewHolder>{

    interface OnStCityClickListener{
        void onStCityClick(StCity state, int position);
    }

    private OnStCityClickListener onClickListener;
    DateTimeFormatter formatq = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final LayoutInflater inflater;
    private final List<StCity> states;

    StCityAdapter(Context context, List<StCity> states, OnStCityClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.states = states;
        this.inflater = LayoutInflater.from(context);
    }

    public void SetOnCl(OnStCityClickListener onClickListener){ this.onClickListener = onClickListener;}
    @Override
    public StCityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StCityAdapter.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        StCity state = states.get(position);
        holder.nameView.setText(state.getName());
        holder.tempView.setText("t воздуха: "+state.getTemp());

        if (state.getSyncDate()!= null){
            holder.syncDateView.setText("Обновлено "+state.getSyncDate().format(formatq));
        }

        int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                 onClickListener.onStCityClick(state, finalPosition);
                if (state.getSyncDate()!= null){
                    holder.tempView.setText("Обновляем данные");
                    holder.syncDateView.setText("Обновляем данные");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return states.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView nameView, tempView, syncDateView;
        ViewHolder(View view){
            super(view);
            nameView = view.findViewById(R.id.name);
            tempView = view.findViewById(R.id.temp);
            syncDateView = view.findViewById(R.id.syncDate);
        }
    }
}
