package com.prod.artchain.ui.examiner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Competitor;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.service.EvaluationApiService;
import com.prod.artchain.data.service.CompetitorApiService;
import com.prod.artchain.data.local.TokenManager;
import com.squareup.picasso.Picasso;

public class EvaluationActivity extends AppCompatActivity {

    public static final String EXTRA_SUBMISSION = "extra_submission";

    private EditText scoreEditText;
    private EditText feedbackEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Submission submission = (Submission) getIntent().getSerializableExtra(EXTRA_SUBMISSION);
        if (submission == null) {
            finish();
            return;
        }

        TextView titleView = findViewById(R.id.evaluationTitle);
        titleView.setText("Evaluate: " + submission.getTitle());

        ImageView paintingImageView = findViewById(R.id.paintingImageView);
        TextView competitorInfoTextView = findViewById(R.id.competitorInfoTextView);

        // Load painting image
        if (submission.getImageUrl() != null && !submission.getImageUrl().isEmpty()) {
            Picasso.get().load(submission.getImageUrl()).into(paintingImageView);
        } else {
            paintingImageView.setImageResource(android.R.color.darker_gray);
        }

        // Set competitor info
        CompetitorApiService.getInstance().getCompetitorByIdAsync(submission.getCompetitorId(), new CompetitorApiService.CompetitorCallback() {
            @Override
            public void onSuccess(Competitor competitor) {
                runOnUiThread(() -> {
                    competitorInfoTextView.setText("Competitor: " + competitor.getFullName() + "\nGrade: " + competitor.getGrade() + "\nSchool: " + competitor.getSchoolName());
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    competitorInfoTextView.setText("Competitor ID: " + submission.getCompetitorId() + " (Failed to load details)");
                });
            }
        });

        scoreEditText = findViewById(R.id.scoreEditText);
        feedbackEditText = findViewById(R.id.feedbackEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> submitEvaluation(submission));
    }

    private void submitEvaluation(Submission submission) {
        String scoreStr = scoreEditText.getText().toString().trim();
        String feedback = feedbackEditText.getText().toString().trim();

        if (scoreStr.isEmpty()) {
            Toast.makeText(this, "Please enter a score", Toast.LENGTH_SHORT).show();
            return;
        }

        int score;
        try {
            score = Integer.parseInt(scoreStr);
            if (score < 1 || score > 10) {
                Toast.makeText(this, "Score must be between 1 and 10", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid score", Toast.LENGTH_SHORT).show();
            return;
        }

        String examinerId = TokenManager.getInstance(this).getUser().getUserId();

        EvaluationApiService.getInstance().submitEvaluationAsync(submission.getPaintingId(), examinerId, score, feedback, new EvaluationApiService.EvaluationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EvaluationActivity.this, "Evaluation submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EvaluationActivity.this, "Failed to submit evaluation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
