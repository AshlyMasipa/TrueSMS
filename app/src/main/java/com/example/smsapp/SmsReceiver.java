package com.example.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.smsapp.utils.PhishingDetector;
import com.example.smsapp.utils.SmsHelper;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMS_FLOW", "=== STEP 1: SMS Received ===");
        Log.d("SMS_FLOW", "Action: " + intent.getAction());

        Log.d(TAG, "Broadcast received: " + intent.getAction());

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e(TAG, "Bundle is null");
                return;
            }

            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) {
                Log.e(TAG, "PDUs array is null");
                return;
            }

            Log.d(TAG, "Processing " + pdus.length + " PDUs");

            StringBuilder messageBody = new StringBuilder();
            String sender = null;

            for (Object pdu : pdus) {
                try {
                    // Use the fully qualified class name to avoid confusion
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    if (sender == null) {
                        sender = sms.getDisplayOriginatingAddress();
                    }
                    messageBody.append(sms.getMessageBody());
                    Intent refreshIntent = new Intent("NEW_SMS_RECEIVED");
                    context.sendBroadcast(refreshIntent);

                } catch (Exception e) {
                    Log.e(TAG, "Error processing SMS PDU", e);
                }
            }

            if (sender != null) {
                final String finalSender = sender;
                final String finalMessageBody = messageBody.toString();

                Log.d(TAG, "SMS from: " + finalSender + ", Body: " + finalMessageBody);

                // Show toast for visual confirmation
                Toast.makeText(context, "SMS received from: " + finalSender, Toast.LENGTH_LONG).show();

                // Check for phishing
                Log.d("SMS_FLOW", "Starting phishing detection");
                PhishingDetector.checkMessage(finalMessageBody, new PhishingDetector.PhishingDetectionCallback() {
                    @Override
                    public void onDetectionResult(boolean isPhishing, double confidence) {
                        Log.d("SMS_FLOW", "Phishing detection result: " + isPhishing + " (" + confidence + ")");

                        // Add to system database
                        Log.d("SMS_FLOW", "Adding to database: " + finalSender + " - " + finalMessageBody);
                        SmsHelper.addMessageToSystemDatabase(context, finalSender, finalMessageBody, false);
                        Log.d("SMS_FLOW", "Database add completed");

                        // Send broadcast to update MainActivity with phishing info
                        Log.d("SMS_FLOW", "Sending broadcast to MainActivity");
                        sendRefreshBroadcast(context, finalSender, finalMessageBody, isPhishing, confidence);

                        // Show warning for phishing messages
                        if (isPhishing) {
                            String warning = "⚠️ Potential phishing detected! (" +
                                    String.format("%.0f%%", confidence * 100) + " confidence)";
                            Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "PHISHING DETECTED: " + warning);
                        }

                        Log.d(TAG, "SMS processed successfully - Phishing: " + isPhishing);
                    }

                    @Override
                    public void onDetectionError(String error) {
                        Log.e("SMS_FLOW", "Phishing detection error: " + error);

                        // Add to system database
                        Log.d("SMS_FLOW", "Adding to database (fallback): " + finalSender);
                        SmsHelper.addMessageToSystemDatabase(context, finalSender, finalMessageBody, false);
                        Log.d("SMS_FLOW", "Database add completed");

                        // Send broadcast to update MainActivity
                        Log.d("SMS_FLOW", "Sending broadcast to MainActivity (fallback)");
                        sendRefreshBroadcast(context, finalSender, finalMessageBody, false, 0.0);

                        Log.d(TAG, "SMS processed with detection error: " + error);
                    }
                });
            }
        }
    }

    private void sendRefreshBroadcast(Context context, String sender, String message, boolean isPhishing, double confidence) {
        Intent refreshIntent = new Intent("NEW_SMS_RECEIVED");
        refreshIntent.putExtra("sender", sender);
        refreshIntent.putExtra("message", message);
        refreshIntent.putExtra("timestamp", System.currentTimeMillis());
        refreshIntent.putExtra("isPhishing", isPhishing);
        refreshIntent.putExtra("phishingConfidence", confidence);
        context.sendBroadcast(refreshIntent);
        Log.d(TAG, "Refresh broadcast sent for: " + sender + " - Phishing: " + isPhishing);
    }
}