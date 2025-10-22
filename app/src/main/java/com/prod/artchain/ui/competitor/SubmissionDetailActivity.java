package com.prod.artchain.ui.competitor;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Evaluation;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.service.EvaluationApiService;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SubmissionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SUBMISSION = "extra_submission";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_detail);

        // Set title
        setTitle("Submission Details");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Submission submission = (Submission) getIntent().getSerializableExtra(EXTRA_SUBMISSION);
        if (submission == null) {
            finish();
            return;
        }

        displaySubmissionDetails(submission);
    }

    private void displaySubmissionDetails(Submission submission) {
        ImageView imageView = findViewById(R.id.detailSubmissionImage);
        TextView titleView = findViewById(R.id.detailSubmissionTitle);
        TextView descriptionView = findViewById(R.id.detailSubmissionDescription);
        TextView contestView = findViewById(R.id.detailSubmissionContest);
        TextView statusView = findViewById(R.id.detailSubmissionStatus);
        TextView dateView = findViewById(R.id.detailSubmissionDate);

        // Load image
        if (submission.getImageUrl() != null && !submission.getImageUrl().isEmpty()) {
            Picasso.get().load(submission.getImageUrl()).into(imageView);
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }

        // Title
        titleView.setText(submission.getTitle() != null ? submission.getTitle() : getString(R.string.submission_title_default));

        // Description
        descriptionView.setText(submission.getDescription() != null ? submission.getDescription() : getString(R.string.no_description));

        // Contest
        String contestName = getString(R.string.n_a);
        if (submission.getContest() != null && submission.getContest().getTitle() != null) {
            contestName = submission.getContest().getTitle();
        }
        contestView.setText(getString(R.string.contest_label, contestName));

        // Status
        statusView.setText(getString(R.string.status_label, submission.getStatus() != null ? submission.getStatus() : getString(R.string.n_a)));

        // Date
        String dateText = getString(R.string.n_a);
        if (submission.getSubmissionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            dateText = sdf.format(submission.getSubmissionDate());
        }
        dateView.setText(getString(R.string.submitted_format, dateText));

        // Load evaluations
        loadEvaluations(submission.getPaintingId());
    }

    private void loadEvaluations(String paintingId) {
        ListView evaluationsListView = findViewById(R.id.evaluationsListView);

        EvaluationApiService.getInstance().getEvaluationForPainting(paintingId, new EvaluationApiService.EvaluationListCallback() {
            @Override
            public void onSuccess(List<Evaluation> evaluations) {
                runOnUiThread(() -> {
                    EvaluationAdapter adapter = new EvaluationAdapter(SubmissionDetailActivity.this, evaluations);
                    evaluationsListView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(SubmissionDetailActivity.this, "Failed to load evaluations: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
