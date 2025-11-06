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

public class AuthApiService {
    public interface LoginCallback {
        void onSuccess(LoggedInUser user);

        void onError(Exception e);
    }

    public interface RegisterCallback {
        void onSuccess(String message);

        void onError(Exception e);
    }

    // Singleton instance for reusability
    private static AuthApiService instance;

    private AuthApiService() {
    }

    public static AuthApiService getInstance() {
        if (instance == null)
            instance = new AuthApiService();
        return instance;
    }

    private LoggedInUser parseUserFromJson(JSONObject json, String accessToken) throws Exception {
        String userId = json.getString("userId");
        String fullName = json.getString("fullName");
        String email = json.optString("email", null);
        String phone = json.optString("phone", null);
        String birthdayStr = json.optString("birthday", null);
        Date birthday = null;
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            birthday = sdf.parse(birthdayStr);
        }
        String ward = json.optString("ward", null);
        String grade = json.optString("grade", null);
        String schoolName = json.optString("schoolName", null);
        String roleStr = json.optString("role", null);
        UserRole role = null;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.d("Error role", Objects.requireNonNull(e.getMessage()));
        }
        return LoggedInUser.builder()
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .birthday(birthday)
                .ward(ward)
                .grade(grade)
                .schoolName(schoolName)
                .role(role)
                .accessToken(accessToken)
                .build();
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

    public void registerAsync(String username, String password, String fullName,
            String email, String role, Date birthday,
            String schoolName, String ward, String grade,
            RegisterCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("fullName", fullName);
            jsonBody.put("email", email);
            jsonBody.put("role", role);

            // Format birthday to ISO 8601 format
            if (birthday != null) {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                jsonBody.put("birthday", isoFormat.format(birthday));
            }

            jsonBody.put("schoolName", schoolName);
            jsonBody.put("ward", ward);
            jsonBody.put("grade", grade);

            HttpClient.getInstance().post("/auth/register", jsonBody, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.optString("message", "Registration successful");
                        callback.onSuccess(message);
                    } catch (JSONException e) {
                        callback.onError(new Exception("Failed to parse registration response: " + e.getMessage(), e));
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
}
