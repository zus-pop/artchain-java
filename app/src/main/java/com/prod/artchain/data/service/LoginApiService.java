package com.prod.artchain.data.service;

import android.util.Log;

import com.prod.artchain.data.model.LoggedInUser;
import com.prod.artchain.data.model.UserRole;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONObject;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class LoginApiService {
    public interface LoginCallback {
        void onSuccess(LoggedInUser user);

        void onError(Exception e);
    }

    // Singleton instance for reusability
    private static LoginApiService instance;

    private LoginApiService() {
    }

    public static LoginApiService getInstance() {
        if (instance == null) instance = new LoginApiService();
        return instance;
    }

    private LoggedInUser parseUserFromJson(JSONObject json, String accessToken) throws Exception {
        String userId = json.getString("userId");
        String fullName = json.getString("fullName");
        String email = json.optString("email", null);
        String phone = json.optString("phone", null);
        String birthdayStr = json.optString("birthday", null);
        Date birthday = null;
        if (birthdayStr != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            birthday = sdf.parse(birthdayStr);
        }
        String ward = json.optString("ward", null);
        String grade = json.optString("grade", null);
        String roleStr = json.optString("role", null);
        UserRole role = null;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.d("Error role", Objects.requireNonNull(e.getMessage()));
        }
        return new LoggedInUser(userId, fullName, email, phone, birthday, ward, grade, role, accessToken);
    }

    public void loginAsync(String username, String password, LoginCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            HttpClient.getInstance().post("/auth/login", jsonBody, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("RES", jsonResponse.toString());
                        String accessToken = jsonResponse.getString("access_token");
                        // Set the token for future requests
                        HttpClient.getInstance().setAuthToken(accessToken);
                        // Fetch user data using getMeAsync
                        getMeAsync(new LoginCallback() {
                            @Override
                            public void onSuccess(LoggedInUser user) {
                                user.setAccessToken(accessToken);
                                callback.onSuccess(user);
                            }

                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }
                        });
                    } catch (JSONException e) {
                        callback.onError(new Exception("Failed to parse login response: " + e.getMessage(), e));
                    }
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } catch (JSONException e) {
            callback.onError(e);
        }
    }

    public void getMeAsync(LoginCallback callback) {
        HttpClient.getInstance().get("/users/me", new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject meJson = new JSONObject(response);
                    // Parse user data from /auth/me response
                    LoggedInUser user = parseUserFromJson(meJson, null);
                    callback.onSuccess(user);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse user data: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
