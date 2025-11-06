package com.prod.artchain.ui.competitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Evaluation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EvaluationAdapter extends BaseAdapter {

    private final Context context;
    private final List<Evaluation> evaluations;

    public EvaluationAdapter(Context context, List<Evaluation> evaluations) {
        this.context = context;
        this.evaluations = evaluations;
    }

    @Override
    public int getCount() {
        return evaluations.size();
    }

    @Override
    public Object getItem(int position) {
        return evaluations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.evaluation_item, parent, false);
        }

        Evaluation evaluation = evaluations.get(position);

        TextView examinerName = convertView.findViewById(R.id.evaluationExaminerName);
        TextView score = convertView.findViewById(R.id.evaluationScore);
        TextView feedback = convertView.findViewById(R.id.evaluationFeedback);
        TextView date = convertView.findViewById(R.id.evaluationDate);

        // Examiner name
        examinerName.setText(evaluation.getExaminerName() != null ? evaluation.getExaminerName() : context.getString(R.string.n_a));

        // Score
        score.setText(context.getString(R.string.score_label, evaluation.getScoreRound1()));

        // Feedback
        feedback.setText(evaluation.getFeedback() != null ? evaluation.getFeedback() : context.getString(R.string.no_feedback));

        // Date
        String dateText = context.getString(R.string.n_a);
        if (evaluation.getEvaluationDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            dateText = sdf.format(evaluation.getEvaluationDate());
        }
        date.setText(context.getString(R.string.evaluated_on, dateText));

        return convertView;
    }
}
