package com.prod.artchain.data.service;

import com.prod.artchain.data.model.Evaluation;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONObject;

public class EvaluationApiService {
    public interface EvaluationCallback {
        void onSuccess(Evaluation evaluation);
        void onError(Exception e);
    }

    // Singleton instance
    private static EvaluationApiService instance;

    private EvaluationApiService() {}

    public static EvaluationApiService getInstance() {
        if (instance == null) instance = new EvaluationApiService();
        return instance;
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
                        Evaluation evaluation = new Evaluation(subId, scr, fb);
                        callback.onSuccess(evaluation);
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
}
