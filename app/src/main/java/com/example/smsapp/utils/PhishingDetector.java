package com.example.smsapp.utils;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PhishingDetector {
    private static final String TAG = "PhishingDetector";
    private static final String API_URL = "https://bubblesoda-phishingdetect.hf.space/predict";

    public interface PhishingDetectionCallback {
        void onDetectionResult(boolean isPhishing, double confidence);
        void onDetectionError(String error);
    }

    public static void checkMessage(String message, PhishingDetectionCallback callback) {
        new PhishingDetectionTask(callback).execute(message);
    }

    private static class PhishingDetectionTask extends AsyncTask<String, Void, DetectionResult> {
        private final PhishingDetectionCallback callback;

        public PhishingDetectionTask(PhishingDetectionCallback callback) {
            this.callback = callback;
        }

        @Override
        protected DetectionResult doInBackground(String... messages) {
            if (messages.length == 0) return new DetectionResult(false, 0.0, "No message provided");

            String message = messages[0];
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);

                // Create JSON request
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("text", message);

                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(jsonRequest.toString().getBytes());
                os.flush();
                os.close();

                // Get response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean isPhishing = jsonResponse.getBoolean("is_phishing");
                    double confidence = jsonResponse.getDouble("confidence");
                    String status = jsonResponse.getString("status");

                    if ("success".equals(status)) {
                        return new DetectionResult(isPhishing, confidence, null);
                    } else {
                        return new DetectionResult(false, 0.0, jsonResponse.getString("status"));
                    }
                } else {
                    return new DetectionResult(false, 0.0, "HTTP error: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error detecting phishing: " + e.getMessage());
                return new DetectionResult(false, 0.0, e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(DetectionResult result) {
            if (result.error == null) {
                callback.onDetectionResult(result.isPhishing, result.confidence);
            } else {
                callback.onDetectionError(result.error);
            }
        }
    }

    private static class DetectionResult {
        boolean isPhishing;
        double confidence;
        String error;

        DetectionResult(boolean isPhishing, double confidence, String error) {
            this.isPhishing = isPhishing;
            this.confidence = confidence;
            this.error = error;
        }
    }
}