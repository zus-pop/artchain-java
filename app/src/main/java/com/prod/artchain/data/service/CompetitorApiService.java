package com.prod.artchain.data.service;

import com.prod.artchain.data.model.Competitor;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONObject;

public class CompetitorApiService {
    public interface CompetitorCallback {
        void onSuccess(Competitor competitor);
        void onError(Exception e);
    }

    // Singleton instance
    private static CompetitorApiService instance;

    private CompetitorApiService() {}

    public static CompetitorApiService getInstance() {
        if (instance == null) instance = new CompetitorApiService();
        return instance;
    }

    public void getCompetitorByIdAsync(String competitorId, CompetitorCallback callback) {
        HttpClient.getInstance().get("/users/" + competitorId, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Competitor competitor = Competitor.builder()
                            .userId(json.optString("userId", ""))
                            .fullName(json.optString("fullName", ""))
                            .grade(json.optString("grade", ""))
                            .schoolName(json.optString("schoolName", ""))
                            .build();
                    callback.onSuccess(competitor);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse competitor: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
