package com.prod.artchain.ui.competitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Submission;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SubmissionAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(Submission submission);
    }

    private final Context context;
    private final List<Submission> submissions;
    private OnItemClickListener listener;

    public SubmissionAdapter(Context context, List<Submission> submissions) {
        this.context = context;
        this.submissions = submissions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return submissions.size();
    }

    @Override
    public Object getItem(int position) {
        return submissions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.submission_item, parent, false);
        }

        Submission submission = submissions.get(position);

        ImageView image = convertView.findViewById(R.id.submissionImage);
        TextView title = convertView.findViewById(R.id.submissionTitle);
        TextView contestTitle = convertView.findViewById(R.id.submissionContestTitle);
        TextView description = convertView.findViewById(R.id.submissionDescription);
        TextView status = convertView.findViewById(R.id.submissionStatus);
        TextView date = convertView.findViewById(R.id.submissionDate);

        // Load submission image
        if (submission.getImageUrl() != null && !submission.getImageUrl().isEmpty()) {
            Picasso.get().load(submission.getImageUrl()).into(image);
        } else {
            image.setImageResource(android.R.color.transparent); // Or set a placeholder
        }

        // Title
        String titleText = submission.getTitle() != null ? submission.getTitle() : context.getString(R.string.submission_title_default);
        title.setText(titleText);

        // Contest title (safe null checks) using resources
        String contestName = context.getString(R.string.n_a);
        if (submission.getContest() != null && submission.getContest().getTitle() != null && !submission.getContest().getTitle().isEmpty()) {
            contestName = submission.getContest().getTitle();
        }
        contestTitle.setText(context.getString(R.string.contest_label, contestName));
        contestTitle.setVisibility(View.VISIBLE);

        // Description
        if (submission.getDescription() != null && !submission.getDescription().isEmpty()) {
            description.setText(submission.getDescription());
        } else {
            description.setText(context.getString(R.string.no_description));
        }

        // Status
        String statusVal = submission.getStatus() != null ? submission.getStatus() : context.getString(R.string.n_a);
        status.setText(context.getString(R.string.status_label, statusVal));

        // Date
        String dateText;
        if (submission.getSubmissionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateText = sdf.format(submission.getSubmissionDate());
        } else {
            dateText = context.getString(R.string.n_a);
        }
        date.setText(context.getString(R.string.submitted_format, dateText));

        // Set click listener to open bottom sheet
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(submission);
            }
        });

        return convertView;
    }
}
