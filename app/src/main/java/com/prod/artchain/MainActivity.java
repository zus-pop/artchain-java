package com.prod.artchain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prod.artchain.data.local.TokenManager;
import com.prod.artchain.data.remote.HttpClient;
import com.prod.artchain.data.service.AuthApiService;
import com.prod.artchain.data.model.LoggedInUser;
import com.prod.artchain.data.model.UserRole;
import com.prod.artchain.ui.competitor.CompetitorActivity;
import com.prod.artchain.ui.examiner.ExaminerActivity;
import com.prod.artchain.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        HttpClient.getInstance().setAuthHandler(() -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                // Clear token and go to login
                TokenManager.getInstance(MainActivity.this).clearToken();
                HttpClient.getInstance().clearAuthToken();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        });

        // Render logged user
        AuthApiService.getInstance().getMeAsync(new AuthApiService.LoginCallback() {
            @Override
            public void onSuccess(LoggedInUser user) {
                runOnUiThread(() -> {
                    // Save user data
                    TokenManager.getInstance(MainActivity.this).saveUser(user);

                    if (user.getRole() == UserRole.COMPETITOR) {
                        Intent intent = new Intent(MainActivity.this, CompetitorActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (user.getRole() == UserRole.EXAMINER) {
                        Intent intent = new Intent(MainActivity.this, ExaminerActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle unknown role or show error
                        // For now, just show a message
                        TextView mainText = findViewById(R.id.loadingTextView);
                        String message = "Unknown role: " + user.getRole();
                        mainText.setText(message);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    TextView mainText = findViewById(R.id.loadingTextView);
                    String message = "Failed to load user data: " + e.getMessage();
                    mainText.setText(message);
                    TokenManager.getInstance(MainActivity.this).clearToken();
                    HttpClient.getInstance().clearAuthToken();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
            }
        });
    }
}