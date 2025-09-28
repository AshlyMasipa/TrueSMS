package com.example.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

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
                Log.d(TAG, "SMS from: " + sender + ", Body: " + messageBody.toString());

                // Show toast for visual confirmation
                Toast.makeText(context, "SMS received from: " + sender, Toast.LENGTH_LONG).show();

                // Add to system database
                Log.d("SMS_FLOW", "Adding to database: " + sender + " - " + messageBody);
                SmsHelper.addMessageToSystemDatabase(context, sender, messageBody.toString(), false);
                Log.d("SMS_FLOW", "Database add completed");

                // Send broadcast to update MainActivity
                Log.d("SMS_FLOW", "Sending broadcast to MainActivity");
                sendRefreshBroadcast(context, sender, messageBody.toString());

                Log.d(TAG, "SMS processed successfully");
            }
        }
    }

    private void sendRefreshBroadcast(Context context, String sender, String message) {
        Intent refreshIntent = new Intent("NEW_SMS_RECEIVED");
        refreshIntent.putExtra("sender", sender);
        refreshIntent.putExtra("message", message);
        refreshIntent.putExtra("timestamp", System.currentTimeMillis());
        context.sendBroadcast(refreshIntent);
        Log.d(TAG, "Refresh broadcast sent for: " + sender);
    }
}