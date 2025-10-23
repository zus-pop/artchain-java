package com.prod.artchain.data.service;

import com.prod.artchain.data.model.Contest;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.remote.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubmissionApiService {
    public interface SubmissionCallback {
        void onSuccess(List<Submission> submissions);

        void onError(Exception e);
    }

    // Singleton instance
    private static SubmissionApiService instance;

    private SubmissionApiService() {
    }

    public static SubmissionApiService getInstance() {
        if (instance == null) instance = new SubmissionApiService();
        return instance;
    }

    private Submission parseSubmissionFromJson(JSONObject json) throws Exception {
        String paintingId = json.getString("paintingId");
        String roundId = json.optString("roundId", null);
        String awardId = json.optString("awardId", null);
        int contestId = json.getInt("contestId");
        String competitorId = json.getString("competitorId");
        String description = json.optString("description", "");
        String title = json.getString("title");
        String imageUrl = json.getString("imageUrl");
        String submissionDateStr = json.getString("submissionDate");
        String status = json.getString("status");
        String createdAtStr = json.getString("createdAt");
        String updatedAtStr = json.getString("updatedAt");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date submissionDate = sdf.parse(submissionDateStr);
        Date createdAt = sdf.parse(createdAtStr);
        Date updatedAt = sdf.parse(updatedAtStr);

        Contest contest = null;
        if (json.has("contest")) {
            JSONObject contestJson = json.getJSONObject("contest");
            contest = parseContestFromJson(contestJson);
        }

        return Submission.builder()
                .paintingId(paintingId)
                .roundId(roundId)
                .awardId(awardId)
                .contestId(contestId)
                .competitorId(competitorId)
                .description(description)
                .title(title)
                .imageUrl(imageUrl)
                .submissionDate(submissionDate)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .contest(contest)
                .build();
    }

    private Contest parseContestFromJson(JSONObject json) throws Exception {
        int contestId = json.getInt("contestId");
        String title = json.getString("title");
        String description = json.optString("description", "");
        String bannerUrl = json.optString("bannerUrl", "");
        int numOfAward = json.optInt("numOfAward", 0);
        String startDateStr = json.getString("startDate");
        String endDateStr = json.getString("endDate");
        String status = json.getString("status");
        String createdBy = json.optString("createdBy", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

        return Contest.builder()
                .contestId(String.valueOf(contestId))
                .title(title)
                .description(description)
                .bannerUrl(bannerUrl)
                .numOfAward(numOfAward)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .createdBy(createdBy)
                .build();
    }

    public void getSubmissionsAsync(SubmissionCallback callback) {
        HttpClient.getInstance().get("/users/me/submissions", new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Submission> submissions = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Submission submission = parseSubmissionFromJson(json);
                        submissions.add(submission);
                    }
                    callback.onSuccess(submissions);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse submissions: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getSubmissionsByContestIdAsync(String contestId, SubmissionCallback callback) {
        HttpClient.getInstance().get("/paintings?contestId=" + contestId, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Submission> submissions = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Submission submission = parseSubmissionFromJson(json);
                        submissions.add(submission);
                    }
                    callback.onSuccess(submissions);
                } catch (Exception e) {
                    callback.onError(new Exception("Failed to parse submissions: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void uploadAsync(String competitorId, String title, String description, String contestId, String roundId, File file, SubmissionCallback callback) {
        Map<String, String> textParts = new HashMap<>();
        textParts.put("competitorId", competitorId);
        textParts.put("title", title);
        if (description != null && !description.isEmpty()) {
            textParts.put("description", description);
        }
        textParts.put("contestId", String.valueOf(contestId));
        textParts.put("roundId", roundId);

        HttpClient.getInstance().postWithFile("/paintings/upload", textParts, "file", file, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                // Assuming success, perhaps parse response or just notify
                callback.onSuccess(new ArrayList<>()); // Empty list or handle accordingly
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
