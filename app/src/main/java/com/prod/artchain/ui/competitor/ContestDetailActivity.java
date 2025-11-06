package com.prod.artchain.ui.competitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.local.TokenManager;
import com.prod.artchain.data.model.Contest;
import com.prod.artchain.data.remote.HttpClient;
import com.prod.artchain.data.service.ContestApiService;
import com.prod.artchain.ui.login.LoginActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ContestDetailActivity extends AppCompatActivity {

    private Contest contest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest_detail);

        // Set title
        setTitle("Contest Details");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get contestId from intent
        String contestId = getIntent().getStringExtra("contestId");

        if (contestId == null || contestId.isEmpty()) {
            Toast.makeText(this, "Contest ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Fetch contest details
        ContestApiService.getInstance().getContestByIdAsync(contestId, new ContestApiService.ContestDetailCallback() {
            @Override
            public void onSuccess(Contest fetchedContest) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    contest = fetchedContest;
                    populateViews();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ContestDetailActivity.this, "Failed to load contest: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateViews() {
        ImageView banner = findViewById(R.id.contestDetailBanner);
        TextView title = findViewById(R.id.contestDetailTitle);
        TextView description = findViewById(R.id.contestDetailDescription);
        TextView dates = findViewById(R.id.contestDetailDates);
        TextView status = findViewById(R.id.contestDetailStatus);
        TextView numAwards = findViewById(R.id.contestDetailNumAwards);
        Button participateButton = findViewById(R.id.participateButton);

        // Load banner
        if (contest.getBannerUrl() != null && !contest.getBannerUrl().isEmpty()) {
            Picasso.get().load(contest.getBannerUrl()).into(banner);
        }

        title.setText(contest.getTitle());
        description.setText(contest.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(contest.getStartDate());
        String endDate = sdf.format(contest.getEndDate());
        dates.setText("Start: " + startDate + " - End: " + endDate);

        status.setText("Status: " + contest.getStatus());
        numAwards.setText("Number of Awards: " + contest.getNumOfAward());

        // Check status for participate button
        if ("ACTIVE".equals(contest.getStatus())) {
            participateButton.setEnabled(true);
            participateButton.setText("Participate");
        } else {
            participateButton.setEnabled(false);
            participateButton.setText("Not Available");
        }

        participateButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra("contestId", contest.getContestId());
                intent.putExtra(
                        "roundId",
                        contest
                                .getRounds()
                                .stream()
                                .filter(
                                        r -> r
                                                .getName()
                                                .equals("ROUND_1"))
                                .findFirst()
                                .orElseThrow()
                                .getRoundId()
                                .toString()
                                ); // Default round
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ContestDetailActivity.this, "Error starting upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.competitor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_my_submissions) {
            openMySubmissions();
            return true;
        } else if (item.getItemId() == R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openMySubmissions() {
        Intent intent = new Intent(this, MySubmissionsActivity.class);
        startActivity(intent);
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

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
