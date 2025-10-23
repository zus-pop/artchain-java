package com.prod.artchain.ui.examiner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Submission;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SubmissionAdapter extends ArrayAdapter<Submission> {

    private final Context context;
    private final List<Submission> submissions;

    public SubmissionAdapter(@NonNull Context context, @NonNull List<Submission> submissions) {
        super(context, 0, submissions);
        this.context = context;
        this.submissions = submissions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.submission_item, parent, false);
        }

        Submission submission = submissions.get(position);

        ImageView imageView = convertView.findViewById(R.id.submissionImage);
        TextView titleView = convertView.findViewById(R.id.submissionTitle);
        TextView contestTitleView = convertView.findViewById(R.id.submissionContestTitle);
        TextView descriptionView = convertView.findViewById(R.id.submissionDescription);
        TextView statusView = convertView.findViewById(R.id.submissionStatus);
        TextView dateView = convertView.findViewById(R.id.submissionDate);

        // Load image
        if (submission.getImageUrl() != null && !submission.getImageUrl().isEmpty()) {
            Picasso.get().load(submission.getImageUrl()).into(imageView);
        } else {
            imageView.setImageResource(android.R.color.darker_gray);
        }

        titleView.setText(submission.getTitle() != null ? submission.getTitle() : "No Title");

        // Contest title
        String contestName = "N/A";
        if (submission.getContest() != null && submission.getContest().getTitle() != null) {
            contestName = submission.getContest().getTitle();
        }
        contestTitleView.setText("Contest: " + contestName);

        descriptionView.setText(submission.getDescription() != null ? submission.getDescription() : "No Description");
        statusView.setText("Status: " + (submission.getStatus() != null ? submission.getStatus() : "Unknown"));

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateText = submission.getSubmissionDate() != null ? sdf.format(submission.getSubmissionDate()) : "N/A";
        dateView.setText("Submitted: " + dateText);

        return convertView;
    }
}
