package com.smallangrycoders.nevermorepayforwater;


import android.content.Context;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

//private static final String USER_AGENT = ;

public class ApiService {
    private OkHttpClient client;
    private Context context;

    private static volatile ApiService instance;

    private ApiService(Context context) {
        this.client = new OkHttpClient();
        this.context = context;
    }

    private ApiService() {
    }

    public static ApiService getInstance(Context context){
        ApiService localInstance = instance;
        if (localInstance == null) {
            synchronized (ApiService.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ApiService(context);
                }
            }
        }
        return localInstance;
    }


    public interface WeatherCallback {
        void onSuccess(String temperature);
        void onFailure(String error);
    }

    public interface GeocodingCallback {
        void onSuccess(String lat, String lon);
        void onFailure(String error);
    }

    public void getWeather(StCity state, WeatherCallback callback) {
        String foreAddr = context.getString(R.string.forecast_addr);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(foreAddr +
                context.getString(R.string.lat_condition) + state.getStrLat() +
                context.getString(R.string.lon_condition) + state.getStrLon() +
                context.getString(R.string.add_condition)).newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().maxStale(3, TimeUnit.SECONDS).build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(context.getString(R.string.err_text));
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject jo = new JSONObject(responseData);
                    String tempFromAPI = jo.getJSONObject(context.getString(R.string.cur_weather))
                            .get(context.getString(R.string.temperature)).toString();
                    callback.onSuccess(tempFromAPI);
                } catch (JSONException e) {
                    callback.onFailure(context.getString(R.string.err_text));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(context.getString(R.string.err_connect));
            }
        });
    }

    public void getCoordinates(String locationName, GeocodingCallback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://nominatim.openstreetmap.org/search").newBuilder()
                .addQueryParameter("q", locationName)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "NeverMorePayForWater/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(context.getString(R.string.geocode_failed) + ": " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);
                    if (jsonArray.length() > 0) {
                        JSONObject firstResult = jsonArray.getJSONObject(0);
                        String lat = firstResult.getString("lat");
                        String lon = firstResult.getString("lon");
                        callback.onSuccess(lat, lon);
                    } else {
                        callback.onFailure(context.getString(R.string.location_not_found));
                    }
                } catch (JSONException e) {
                    callback.onFailure(context.getString(R.string.parse_error));
                }
            }
        });
    }

}
