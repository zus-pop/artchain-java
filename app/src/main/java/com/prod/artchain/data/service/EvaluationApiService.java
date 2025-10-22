package com.prod.artchain.data.service;

import com.prod.artchain.data.model.Evaluation;
import com.prod.artchain.data.model.EvaluationRequest;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvaluationApiService {
    public interface EvaluationCallback {
        void onSuccess(EvaluationRequest evaluationRequest);
        void onError(Exception e);
    }

    public interface EvaluationListCallback {
        void onSuccess(List<Evaluation> evaluations);
        void onError(Exception e);
    }

    // Singleton instance
    private static EvaluationApiService instance;

    private EvaluationApiService() {}

    public static EvaluationApiService getInstance() {
        if (instance == null) instance = new EvaluationApiService();
        return instance;
    }

    private Evaluation parseEvaluationFromJson(JSONObject json) throws Exception {
        String id = json.getString("id");
        String paintingId = json.getString("paintingId");
        String examinerId = json.getString("examinerId");
        String examinerName = json.getString("examinerName");
        int score = json.getInt("score");
        String feedback = json.getString("feedback");
        String evaluationDateStr = json.getString("evaluationDate");
        String status = json.getString("status");
        String createdAtStr = json.getString("createdAt");
        String updatedAtStr = json.getString("updatedAt");

        // Parse dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date evaluationDate = sdf.parse(evaluationDateStr);
        Date createdAt = sdf.parse(createdAtStr);
        Date updatedAt = sdf.parse(updatedAtStr);

        return Evaluation.builder()
                .id(id)
                .paintingId(paintingId)
                .examinerId(examinerId)
                .examinerName(examinerName)
                .score(score)
                .feedback(feedback)
                .evaluationDate(evaluationDate)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void evaluateAsync(String submissionId, int score, String feedback, EvaluationCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("submissionId", submissionId);
            jsonBody.put("score", score);
            jsonBody.put("feedback", feedback);

            HttpClient.getInstance().post("/evaluations", jsonBody, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        String subId = json.getString("submissionId");
                        int scr = json.getInt("score");
                        String fb = json.getString("feedback");
                        EvaluationRequest evaluationRequest = new EvaluationRequest(subId, scr, fb);
                        callback.onSuccess(evaluationRequest);
                    } catch (Exception e) {
                        callback.onError(new Exception("Failed to parse evaluation: " + e.getMessage(), e));
                    }
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }


    public void getEvaluationForPainting(String paintingId, EvaluationListCallback callback) {
        HttpClient.getInstance().get("/paintings/" + paintingId + "/evaluations", new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Evaluation> evaluations = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Evaluation evaluation = parseEvaluationFromJson(json);
                        evaluations.add(evaluation);
                    }
                    callback.onSuccess(evaluations);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse evaluations: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
