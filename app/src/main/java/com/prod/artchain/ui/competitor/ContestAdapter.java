package com.prod.artchain.ui.competitor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Contest;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContestAdapter extends BaseAdapter {

    private final Context context;
    private final List<Contest> contests;

    public ContestAdapter(Context context, List<Contest> contests) {
        this.context = context;
        this.contests = contests;
    }

    @Override
    public int getCount() {
        return contests.size();
    }

    @Override
    public Object getItem(int position) {
        return contests.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contest_item, parent, false);
        }

        Contest contest = contests.get(position);

        ImageView banner = convertView.findViewById(R.id.contestBanner);
        TextView description = convertView.findViewById(R.id.contestDescription);
        TextView dates = convertView.findViewById(R.id.contestDates);
        TextView status = convertView.findViewById(R.id.contestStatus);

        // Load banner image
        if (contest.getBannerUrl() != null && !contest.getBannerUrl().isEmpty()) {
            Picasso.get().load(contest.getBannerUrl()).into(banner);
        } else {
            banner.setImageResource(android.R.color.transparent); // Or set a placeholder
        }

        description.setText(contest.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(contest.getStartDate());
        String endDate = sdf.format(contest.getEndDate());
        String dateText = "Start: " + startDate + " - End: " + endDate;
        dates.setText(dateText);

        String statusText = "Status: " + contest.getStatus();
        status.setText(statusText);

        // Set click listener to open detail
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContestDetailActivity.class);
            intent.putExtra("contestId", contest.getContestId());
            context.startActivity(intent);
        });

        return convertView;
    }
}
