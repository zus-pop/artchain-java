package com.prod.artchain.ui.examiner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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

public class ExaminerActivity extends AppCompatActivity {

    private ListView submissionListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examiner);

        // Set title
        setTitle("Submissions");

        // Set auth handler for token expiration
        HttpClient.getInstance().setAuthHandler(() -> {
            runOnUiThread(() -> {
                Toast.makeText(ExaminerActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                // Clear token and go to login
                TokenManager.getInstance(ExaminerActivity.this).clearToken();
                HttpClient.getInstance().clearAuthToken();
                startActivity(new Intent(ExaminerActivity.this, LoginActivity.class));
                finish();
            });
        });

        submissionListView = findViewById(R.id.submissionListView);

        loadSubmissions();
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

    private void loadSubmissions() {
        SubmissionApiService.getInstance().getSubmissionsAsync(new SubmissionApiService.SubmissionCallback() {
            @Override
            public void onSuccess(List<Submission> submissions) {
                runOnUiThread(() -> {
                    ArrayAdapter<Submission> adapter = new ArrayAdapter<>(ExaminerActivity.this,
                            android.R.layout.simple_list_item_1, submissions);
                    submissionListView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ExaminerActivity.this, "Failed to load submissions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
