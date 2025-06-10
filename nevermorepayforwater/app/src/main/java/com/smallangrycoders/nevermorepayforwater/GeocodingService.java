package com.smallangrycoders.nevermorepayforwater;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingService {
    private final Context context;
    private final OkHttpClient client;
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/search";

    public interface GeocodingCallback {
        void onSuccess(double latitude, double longitude);
        void onError(String message);
    }

    public GeocodingService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "NeverMorePayForWater Android App")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    public void getCoordinates(String cityName, GeocodingCallback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NOMINATIM_BASE_URL).newBuilder()
                .addQueryParameter("q", cityName)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    callback.onError(context.getString(R.string.err_connect))
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(context.getString(R.string.err_text))
                    );
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONArray results = new JSONArray(responseData);
                    
                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        
                        new Handler(Looper.getMainLooper()).post(() ->
                            callback.onSuccess(lat, lon)
                        );
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError(context.getString(R.string.err_no_results))
                        );
                    }
                } catch (JSONException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(context.getString(R.string.err_text))
                    );
                }
            }
        });
    }
} 