package com.prod.artchain.data.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.prod.artchain.data.model.Ward;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WardApiService {
    private static final String TAG = "WardApiService";
    private static final String WARD_API_URL = "https://provinces.open-api.vn/api/v2/w/?province=79";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface WardCallback {
        void onSuccess(List<Ward> wards);

        void onError(String error);
    }

    public static void fetchWards(final WardCallback callback) {
        executor.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(WARD_API_URL)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onError("Failed to fetch wards: " + response.code()));
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Response: " + responseBody);

                // API trả về trực tiếp JSONArray chứa danh sách wards
                JSONArray wardsArray = new JSONArray(responseBody);

                List<Ward> allWards = new ArrayList<>();

                // Parse từng ward
                for (int i = 0; i < wardsArray.length(); i++) {
                    JSONObject wardJson = wardsArray.getJSONObject(i);
                    Ward ward = Ward.builder()
                            .name(wardJson.getString("name"))
                            .code(wardJson.getInt("code"))
                            .divisionType(wardJson.optString("division_type", ""))
                            .codename(wardJson.optString("codename", ""))
                            .provinceCode(79) // Hardcode province code 79 for Ho Chi Minh City
                            .build();
                    allWards.add(ward);
                }

                Log.d(TAG, "Loaded " + allWards.size() + " wards");
                mainHandler.post(() -> callback.onSuccess(allWards));
            } catch (IOException e) {
                Log.e(TAG, "Network error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing ward data", e);
                mainHandler.post(() -> callback.onError("Error parsing data: " + e.getMessage()));
            }
        });
    }
}
