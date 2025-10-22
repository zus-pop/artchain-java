package com.prod.artchain.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.*;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static HttpClient instance;
    private final OkHttpClient client;
    private final String baseUrl;
    private final Map<String, String> defaultHeaders;
    private AuthHandler authHandler;

    private HttpClient() {
        // Add logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        defaultHeaders = new HashMap<>();
        // Set default headers, e.g., Content-Type
        defaultHeaders.put("Content-Type", "application/json");
        this.baseUrl = "https://rflz4357-3001.asse.devtunnels.ms/api";
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }


    public void addDefaultHeader(String key, String value) {
        defaultHeaders.put(key, value);
    }

    public void removeDefaultHeader(String key) {
        defaultHeaders.remove(key);
    }

    public interface HttpCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }

    public void get(String endpoint, HttpCallback callback) {
        get(endpoint, null, callback);
    }

    public void get(String endpoint, Map<String, String> headers, HttpCallback callback) {
        Request.Builder builder = new Request.Builder().url(baseUrl + endpoint).get();
        addHeaders(builder, headers);
        executeRequest(builder.build(), callback);
    }

    public void post(String endpoint, JSONObject jsonBody, HttpCallback callback) {
        post(endpoint, jsonBody, null, callback);
    }

    public void post(String endpoint, JSONObject jsonBody, Map<String, String> headers, HttpCallback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request.Builder builder = new Request.Builder().url(baseUrl + endpoint).post(body);
        addHeaders(builder, headers);
        executeRequest(builder.build(), callback);
    }

    public void postWithFile(String endpoint, Map<String, String> textParts, String fileKey, File file, HttpCallback callback) {
        postWithFile(endpoint, textParts, fileKey, file, null, callback);
    }

    public void postWithFile(String endpoint, Map<String, String> textParts, String fileKey, File file, Map<String, String> headers, HttpCallback callback) {
        RequestBody requestBody = buildMultipartBody(textParts, fileKey, file);
        Request.Builder builder = new Request.Builder().url(baseUrl + endpoint).post(requestBody);
        addHeaders(builder, headers);
        executeRequest(builder.build(), callback);
    }

    private RequestBody buildMultipartBody(Map<String, String> textParts, String fileKey, File file) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, String> entry : textParts.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        if (file != null) {
            String mimeType = getMimeType(file.getPath());
            RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
            builder.addFormDataPart(fileKey, file.getName(), fileBody);
        }

        return builder.build();
    }

    private String getMimeType(String path) {
        String extension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }

    // Add more methods like put, delete as needed

    private void addHeaders(Request.Builder builder, Map<String, String> additionalHeaders) {
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private void executeRequest(Request request, HttpCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    callback.onSuccess(responseBody);
                } else {
                    callback.onError(new Exception("HTTP " + response.code() + ": " + response.message()));
                    if (response.code() == 401 && authHandler != null) {
                        authHandler.onUnauthorized();
                    }
                }
            }
        });
    }

    public void setAuthToken(String token) {
        addDefaultHeader("Authorization", "Bearer " + token);
    }

    public void clearAuthToken() {
        removeDefaultHeader("Authorization");
    }

    public interface AuthHandler {
        void onUnauthorized();
    }

    public void setAuthHandler(AuthHandler handler) {
        this.authHandler = handler;
    }
}
