package com.example.smsapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SmsAppService extends Service {
    private static final String TAG = "SmsAppService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SMS App Service started");
        return START_STICKY;
    }
}