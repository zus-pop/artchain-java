package com.prod.artchain.ui.examiner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Contest;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.remote.HttpClient;
import com.prod.artchain.data.service.ContestApiService;
import com.prod.artchain.data.service.SubmissionApiService;
import com.prod.artchain.ui.login.LoginActivity;
import com.prod.artchain.data.local.TokenManager;

import java.util.List;

public class ExaminerActivity extends AppCompatActivity {

    private ListView listView;
    private List<Contest> contests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examiner);

        // Set title
        setTitle("Contests to Evaluate");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        listView = findViewById(R.id.submissionListView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Contest selectedContest = contests.get(position);
            loadSubmissionsForContest(selectedContest.getContestId());
        });

        loadContests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button
            loadContests();
            setTitle("Contests to Evaluate");
            return true;
        }
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
        String examinerId = TokenManager.getInstance(this).getUser().getUserId();
        ContestApiService.getInstance().getContestsByExaminerIdAsync(examinerId, new ContestApiService.ContestCallback() {
            @Override
            public void onSuccess(List<Contest> contestList) {
                contests = contestList;
                runOnUiThread(() -> {
                    ContestAdapter adapter = new ContestAdapter(ExaminerActivity.this, contests);
                    listView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ExaminerActivity.this, "Failed to load contests: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadSubmissionsForContest(String contestId) {
        SubmissionApiService.getInstance().getSubmissionsByContestIdAsync(contestId, new SubmissionApiService.SubmissionCallback() {
            @Override
            public void onSuccess(List<Submission> submissions) {
                runOnUiThread(() -> {
                    SubmissionAdapter adapter = new SubmissionAdapter(ExaminerActivity.this, submissions);
                    listView.setAdapter(adapter);
                    setTitle("Submissions for Contest");

                    // Set click listener for submissions
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        Submission selectedSubmission = submissions.get(position);
                        Intent intent = new Intent(ExaminerActivity.this, EvaluationActivity.class);
                        intent.putExtra(EvaluationActivity.EXTRA_SUBMISSION, selectedSubmission);
                        startActivity(intent);
                    });
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
