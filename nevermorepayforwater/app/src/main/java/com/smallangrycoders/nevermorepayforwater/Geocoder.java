package com.smallangrycoders.nevermorepayforwater;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;

import okhttp3.*;

public final class Geocoder {

    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "NeverMorePayForWater Android App";

    private final Context appContext;
    private final OkHttpClient httpClient;

    public interface ResultCallback {
        void onLocationFound(double latitude, double longitude);
        void onFailure(String errorMessage);
    }

    public Geocoder(Context context) {
        this.appContext = context.getApplicationContext();
        this.httpClient = createHttpClient();
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(this::addUserAgentHeader)
                .build();
    }

    private Response addUserAgentHeader(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request modifiedRequest = originalRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .build();
        return chain.proceed(modifiedRequest);
    }

    public void fetchCoordinates(String locationName, ResultCallback callback) {
        HttpUrl requestUrl = buildRequestUrl(locationName);
        Request request = new Request.Builder().url(requestUrl).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException error) {
                notifyError(callback, R.string.err_connect);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyError(callback, R.string.err_text);
                    return;
                }
                processResponse(response, callback);
            }
        });
    }

    private HttpUrl buildRequestUrl(String location) {
        return Objects.requireNonNull(HttpUrl.parse(NOMINATIM_API_URL)).newBuilder()
                .addQueryParameter("q", location)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .build();
    }

    private void processResponse(Response response, ResultCallback callback) throws IOException {
        try {
            assert response.body() != null;
            String responseBody = response.body().string();
            JSONArray locations = new JSONArray(responseBody);

            if (locations.length() == 0) {
                notifyError(callback, R.string.err_no_results);
                return;
            }

            JSONObject firstResult = locations.getJSONObject(0);
            double latitude = firstResult.getDouble("lat");
            double longitude = firstResult.getDouble("lon");

            notifySuccess(callback, latitude, longitude);

        } catch (JSONException parseError) {
            notifyError(callback, R.string.err_text);
        }
    }

    private void notifySuccess(ResultCallback callback, double lat, double lon) {
        runOnUiThread(() -> callback.onLocationFound(lat, lon));
    }

    private void notifyError(ResultCallback callback, int errorMessageResId) {
        String message = appContext.getString(errorMessageResId);
        runOnUiThread(() -> callback.onFailure(message));
    }

    private void runOnUiThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}