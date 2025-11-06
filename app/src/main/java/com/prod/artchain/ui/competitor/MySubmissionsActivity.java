package com.prod.artchain.ui.competitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.remote.HttpClient;
import com.prod.artchain.data.service.SubmissionApiService;
import com.prod.artchain.ui.login.LoginActivity;
import com.prod.artchain.data.local.TokenManager;

import java.util.List;

public class MySubmissionsActivity extends AppCompatActivity {

    private ListView submissionsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_submissions);

        // Set title
        setTitle("My Submissions");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set auth handler for token expiration
        HttpClient.getInstance().setAuthHandler(() -> {
            runOnUiThread(() -> {
                Toast.makeText(MySubmissionsActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                // Clear token and go to login
                TokenManager.getInstance(MySubmissionsActivity.this).clearToken();
                HttpClient.getInstance().clearAuthToken();
                startActivity(new Intent(MySubmissionsActivity.this, LoginActivity.class));
                finish();
            });
        });

        submissionsListView = findViewById(R.id.submissionsListView);

        loadSubmissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.competitor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logout();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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

    private void loadSubmissions() {
        SubmissionApiService.getInstance().getSubmissionsAsync(new SubmissionApiService.SubmissionCallback() {
            @Override
            public void onSuccess(List<Submission> submissions) {
                runOnUiThread(() -> {
                    SubmissionAdapter adapter = new SubmissionAdapter(MySubmissionsActivity.this, submissions);
                    adapter.setOnItemClickListener(submission -> {
                        Intent intent = new Intent(MySubmissionsActivity.this, SubmissionDetailActivity.class);
                        intent.putExtra(SubmissionDetailActivity.EXTRA_SUBMISSION, submission);
                        startActivity(intent);
                    });
                    submissionsListView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MySubmissionsActivity.this, "Failed to load submissions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
