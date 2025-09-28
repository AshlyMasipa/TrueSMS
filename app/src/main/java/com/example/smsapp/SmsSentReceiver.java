package com.example.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.smsapp.utils.SmsHelper;

public class SmsSentReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsSentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("SMS_SENT".equals(intent.getAction())) {
            int resultCode = getResultCode();
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String message = intent.getStringExtra("message");

            Log.d(TAG, "SMS send result: " + resultCode + " for " + phoneNumber);

            switch (resultCode) {
                case -1: // Activity.RESULT_OK
                    Log.d(TAG, "SMS sent successfully to: " + phoneNumber);
                    Toast.makeText(context, "Message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

                    // Make sure message is added to database
                    SmsHelper.addMessageToSystemDatabase(context, phoneNumber, message, true);
                    break;

                case 1: // SmsManager.RESULT_ERROR_GENERIC_FAILURE
                    Log.e(TAG, "SMS generic failure");
                    Toast.makeText(context, "Failed to send message: Generic failure", Toast.LENGTH_LONG).show();
                    break;

                case 2: // SmsManager.RESULT_ERROR_RADIO_OFF
                    Log.e(TAG, "SMS radio off");
                    Toast.makeText(context, "Failed to send message: No signal", Toast.LENGTH_LONG).show();
                    break;

                case 3: // SmsManager.RESULT_ERROR_NULL_PDU
                    Log.e(TAG, "SMS null PDU");
                    Toast.makeText(context, "Failed to send message: Invalid format", Toast.LENGTH_LONG).show();
                    break;

                case 4: // SmsManager.RESULT_ERROR_NO_SERVICE
                    Log.e(TAG, "SMS no service");
                    Toast.makeText(context, "Failed to send message: No service", Toast.LENGTH_LONG).show();
                    break;

                default:
                    Log.d(TAG, "SMS send result: " + resultCode);
                    Toast.makeText(context, "Message send status: " + resultCode, Toast.LENGTH_SHORT).show();
                    break;
            }

            // Refresh conversations
            Intent refreshIntent = new Intent("REFRESH_CONVERSATIONS");
            context.sendBroadcast(refreshIntent);
        }
    }
}