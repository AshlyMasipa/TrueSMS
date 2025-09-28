package com.example.smsapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SmsSender {
    private static final String TAG = "SmsSender";

    public static void sendSms(Context context, String phoneNumber, String message, SmsSendCallback callback) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            // Create pending intent for send status
            Intent sentIntent = new Intent("SMS_SENT");
            sentIntent.putExtra("phoneNumber", phoneNumber);
            sentIntent.putExtra("message", message);

            int requestCode = (int) System.currentTimeMillis(); // Unique request code
            PendingIntent sentPI = PendingIntent.getBroadcast(context, requestCode, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Send the message
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
            Log.d(TAG, "SMS sending initiated to: " + phoneNumber);

            // Don't show success message yet - wait for broadcast
            if (callback != null) {
                callback.onSending();
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage(), e);
            if (callback != null) {
                callback.onSendFailed(e.getMessage());
            }
            Toast.makeText(context, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public interface SmsSendCallback {
        void onSending();
        void onSendSuccess();
        void onSendFailed(String error);
    }
}