package com.prod.artchain.ui.competitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Contest;
import com.prod.artchain.data.remote.HttpClient;
import com.prod.artchain.data.service.ContestApiService;
import com.prod.artchain.ui.login.LoginActivity;
import com.prod.artchain.data.local.TokenManager;

import java.util.List;

public class CompetitorActivity extends AppCompatActivity {

    private ListView contestListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competitor);

        // Set title
        setTitle("Contests");

        // Set auth handler for token expiration
        HttpClient.getInstance().setAuthHandler(() -> {
            runOnUiThread(() -> {
                Toast.makeText(CompetitorActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                // Clear token and go to login
                TokenManager.getInstance(CompetitorActivity.this).clearToken();
                HttpClient.getInstance().clearAuthToken();
                startActivity(new Intent(CompetitorActivity.this, LoginActivity.class));
                finish();
            });
        });

        contestListView = findViewById(R.id.contestListView);

        loadContests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear token
        TokenManager.getInstance(this).clearToken();
        HttpClient.getInstance().clearAuthToken();
        // Go to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadContests() {
        ContestApiService.getInstance().getContestsAsync(new ContestApiService.ContestCallback() {
            @Override
            public void onSuccess(List<Contest> contests) {
                runOnUiThread(() -> {
                    ContestAdapter adapter = new ContestAdapter(CompetitorActivity.this, contests);
                    contestListView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CompetitorActivity.this, "Failed to load contests: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
