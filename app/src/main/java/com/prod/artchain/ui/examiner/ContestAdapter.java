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
import com.prod.artchain.data.model.Contest;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContestAdapter extends ArrayAdapter<Contest> {

    private final Context context;
    private final List<Contest> contests;

    public ContestAdapter(@NonNull Context context, @NonNull List<Contest> contests) {
        super(context, 0, contests);
        this.context = context;
        this.contests = contests;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contest_item, parent, false);
        }

        Contest contest = contests.get(position);

        ImageView bannerView = convertView.findViewById(R.id.contestBanner);
        TextView titleView = convertView.findViewById(R.id.contestTitle);
        TextView descriptionView = convertView.findViewById(R.id.contestDescription);
        TextView datesView = convertView.findViewById(R.id.contestDates);
        TextView statusView = convertView.findViewById(R.id.contestStatus);

        // Load banner image
        if (contest.getBannerUrl() != null && !contest.getBannerUrl().isEmpty()) {
            Picasso.get().load(contest.getBannerUrl()).into(bannerView);
        } else {
            bannerView.setImageResource(android.R.color.darker_gray);
        }

        titleView.setText(contest.getTitle() != null ? contest.getTitle() : "No Title");
        descriptionView.setText(contest.getDescription() != null ? contest.getDescription() : "No Description");

        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = contest.getStartDate() != null ? sdf.format(contest.getStartDate()) : "N/A";
        String endDate = contest.getEndDate() != null ? sdf.format(contest.getEndDate()) : "N/A";
        datesView.setText("Start: " + startDate + " - End: " + endDate);

        statusView.setText("Status: " + (contest.getStatus() != null ? contest.getStatus() : "Unknown"));

        return convertView;
    }
}
