package com.prod.artchain.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.prod.artchain.data.model.LoggedInUser;
import com.prod.artchain.data.model.UserRole;

public class TokenManager {
    private static final String PREFS_NAME = "token_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_DATA = "user_data";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void saveUser(LoggedInUser user) {
        SharedPreferences.Editor editor = prefs.edit();
        // Since LoggedInUser is Serializable, we can serialize it
        // But for simplicity, save individual fields or use Gson
        // For now, assume we save as string, but better to use Gson
        // Since we have Lombok, but to keep simple, save userId and fullName
        editor.putString("userId", user.getUserId());
        editor.putString("fullName", user.getFullName());
        editor.putString("email", user.getEmail());
        editor.putString("ward", user.getWard());
        editor.putString("grade", user.getGrade());
        editor.putString("role", user.getRole() != null ? user.getRole().toString() : null);
        editor.apply();
    }

    public LoggedInUser getUser() {
        String userId = prefs.getString("userId", null);
        if (userId == null) return null;
        String fullName = prefs.getString("fullName", null);
        String email = prefs.getString("email", null);
        String ward = prefs.getString("ward", null);
        String grade = prefs.getString("grade", null);
        String roleStr = prefs.getString("role", null);
        UserRole role = null;
        try {
            role = UserRole.valueOf(roleStr);
        } catch (Exception e) {
            // ignore
        }
        return new LoggedInUser(userId, fullName, email, null, null, ward, grade, role, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove("userId");
        editor.remove("fullName");
        editor.remove("email");
        editor.remove("ward");
        editor.remove("grade");
        editor.remove("role");
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
