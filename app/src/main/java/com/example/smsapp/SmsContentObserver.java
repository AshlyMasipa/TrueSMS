package com.example.smsapp;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SmsContentObserver extends ContentObserver {
    private static final String TAG = "SmsContentObserver";
    private final SmsChangeListener listener;

    public interface SmsChangeListener {
        void onSmsDatabaseChanged();
    }

    public SmsContentObserver(Handler handler, SmsChangeListener listener) {
        super(handler);
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange) {
        // Ignore the single parameter version, wait for the two-parameter version
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        Log.d(TAG, "SMS database changed - URI: " + uri + ", selfChange: " + selfChange);

        // Check if this is a relevant SMS change
        if (isSmsRelatedUri(uri)) {
            Log.d(TAG, "Relevant SMS change detected, notifying listener");
            if (listener != null) {
                // Use a small delay to ensure the database operation is complete
                new Handler().postDelayed(() -> {
                    listener.onSmsDatabaseChanged();
                }, 200); // 200ms delay
            }
        }
    }

    private boolean isSmsRelatedUri(Uri uri) {
        if (uri == null) return false;

        String uriString = uri.toString();
        return uriString.contains("sms") ||
                uriString.contains("mms") ||
                uriString.contains("telephony") ||
                uriString.contains("conversations");
    }
}