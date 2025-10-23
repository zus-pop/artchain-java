package com.prod.artchain.data.service;

import com.prod.artchain.data.model.Contest;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContestApiService {
    public interface ContestCallback {
        void onSuccess(List<Contest> contests);
        void onError(Exception e);
    }

    public interface ContestDetailCallback {
        void onSuccess(Contest contest);
        void onError(Exception e);
    }

    // Singleton instance
    private static ContestApiService instance;

    private ContestApiService() {}

    public static ContestApiService getInstance() {
        if (instance == null) instance = new ContestApiService();
        return instance;
    }

    private Contest parseContestFromJson(JSONObject json) throws Exception {
        String id = json.getString("contestId"); // Assuming contestId is the id
        String title = json.getString("title");
        String description = json.optString("description", "");
        String bannerUrl = json.optString("bannerUrl", "");
        int numOfAward = json.optInt("numOfAward", 0);
        String startDateStr = json.getString("startDate");
        String endDateStr = json.getString("endDate");
        String status = json.getString("status");
        String createdBy = json.optString("createdBy", "");
        String roundId = json.optString("roundId", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

    return Contest.builder()
            .contestId(id)
            .title(title)
            .description(description)
            .bannerUrl(bannerUrl)
            .numOfAward(numOfAward)
            .startDate(startDate)
            .endDate(endDate)
            .status(status)
            .createdBy(createdBy)
            .roundId(roundId)
            .build();
    }

    public void getContestsAsync(ContestCallback callback) {
        HttpClient.getInstance().get("/contests", new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Contest> contests = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Contest contest = parseContestFromJson(json);
                        contests.add(contest);
                    }
                    callback.onSuccess(contests);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse contests: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getContestByIdAsync(String contestId, ContestDetailCallback callback) {
        HttpClient.getInstance().get("/contests/" + contestId, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Contest contest = parseContestFromJson(json.get("data") instanceof JSONObject ? json.getJSONObject("data") : json);
                    callback.onSuccess(contest);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse contest: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getContestsByExaminerIdAsync(String examinerId, ContestCallback callback) {
        HttpClient.getInstance().get("/contests/examiner/" + examinerId, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseObj = new JSONObject(response);
                    JSONArray jsonArray = responseObj.getJSONArray("data");
                    List<Contest> contests = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Contest contest = parseContestFromJson(json);
                        contests.add(contest);
                    }
                    callback.onSuccess(contests);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse contests: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
