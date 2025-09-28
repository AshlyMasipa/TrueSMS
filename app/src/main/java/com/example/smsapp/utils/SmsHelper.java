package com.example.smsapp.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;
import com.example.smsapp.models.Conversation;
import com.example.smsapp.SmsSender;
import com.example.smsapp.models.SmsMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsHelper {
    private static final String TAG = "SmsHelper";

    public static List<Conversation> getAllConversations(Context context) {
        List<Conversation> conversations = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Use the correct URI and column names
        Uri uri = Telephony.Sms.CONTENT_URI;


        // Query conversations (threads) from system SMS database
        String[] projection = {
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
        };

        String sortOrder = Telephony.Sms.DATE + " DESC";


        Log.d(TAG, "Querying conversations from: " + uri.toString());

        try (Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                Map<Long, Conversation> conversationMap = new HashMap<>();
                Map<Long, Integer> messageCounts = new HashMap<>();

                Log.d(TAG, "Found " + cursor.getCount() + " conversation threads");

                do {
                    @SuppressLint("Range") long threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID));
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                    Integer count = messageCounts.get(threadId);
                    messageCounts.put(threadId, count == null ? 1 : count + 1);

                    // Store latest message for each thread
                    if (!conversationMap.containsKey(threadId)) {
                        String contactName = ContactUtils.getContactName(context, address);
                        Conversation conv = new Conversation(threadId, contactName, address, body, date, 1);
                        conversationMap.put(threadId, conv);
                    }

                } while (cursor.moveToNext());

                // Create final conversations with correct message counts
                for (Long threadId : conversationMap.keySet()) {
                    Conversation conv = conversationMap.get(threadId);
                    Integer count = messageCounts.get(threadId);

                    if (count != null && count != null) {
                        Conversation finalConv = new Conversation(
                                threadId, conv.getContactName(), conv.getPhoneNumber(),
                                conv.getLastMessage(), conv.getTimestamp(), count
                        );
                        conversations.add(finalConv);
                    }
                }


                // Sort conversations by timestamp DESC (newest first)
                Collections.sort(conversations, (c1, c2) -> {
                    return Long.compare(c2.getTimestamp(), c1.getTimestamp()); // DESC order
                });

                Log.d(TAG, "Sorted " + conversations.size() + " conversations by timestamp");
            }
        } catch (Exception e) {
            Log.e("SmsHelper", "Error getting conversations", e);
        }

        return conversations;
    }

    public static List<SmsMessage> getMessagesForThread(Context context, long threadId) {
        List<SmsMessage> messages = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = Telephony.Sms.CONTENT_URI;
        String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ
        };

        String selection = Telephony.Sms.THREAD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(threadId)};
        String sortOrder = Telephony.Sms.DATE + " ASC";

        try (Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
                    boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1;

                    boolean isSent = (type == Telephony.Sms.MESSAGE_TYPE_SENT);
                    String contactName = ContactUtils.getContactName(context, address);

                    messages.add(new SmsMessage(threadId, address, contactName, body, date, isSent, isRead));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("SmsHelper", "Error getting messages for thread: " + threadId, e);
        }

        return messages;
    }

    private static String getAddressForThread(Context context, long threadId) {
        ContentResolver contentResolver = context.getContentResolver();
        String address = "";

        Uri uri = Telephony.Sms.CONTENT_URI;
        String[] projection = {Telephony.Sms.ADDRESS};
        String selection = Telephony.Sms.THREAD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(threadId)};
        String sortOrder = Telephony.Sms.DATE + " DESC LIMIT 1";

        try (Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
            }
        } catch (Exception e) {
            Log.e("SmsHelper", "Error getting address for thread", e);
        }

        return address;
    }

    public static void sendSms(Context context, String phoneNumber, String message) {
        // Send via SmsManager for actual transmission
        SmsSender.SmsSendCallback callback = null;
        SmsSender.sendSms(context, phoneNumber, message, null);

        // Also add to system SMS database so it appears immediately
        addMessageToSystemDatabase(context, phoneNumber, message, true);
    }

    public static void addMessageToSystemDatabase(Context context, String phoneNumber, String message, boolean isSent) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(Telephony.Sms.ADDRESS, phoneNumber);
        values.put(Telephony.Sms.BODY, message);
        values.put(Telephony.Sms.DATE, System.currentTimeMillis());
        values.put(Telephony.Sms.READ, 1);
        values.put(Telephony.Sms.TYPE, isSent ?
                Telephony.Sms.MESSAGE_TYPE_SENT : Telephony.Sms.MESSAGE_TYPE_INBOX);

        try {
            Uri result = contentResolver.insert(Telephony.Sms.CONTENT_URI, values);
            Log.d("SmsHelper", "Message added to system database: " + result);
        } catch (Exception e) {
            Log.e("SmsHelper", "Error adding message to system database", e);
        }
    }

    public static void markMessageAsRead(Context context, long threadId) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.READ, 1);

        String selection = Telephony.Sms.THREAD_ID + " = ? AND " + Telephony.Sms.READ + " = ?";
        String[] selectionArgs = {String.valueOf(threadId), "0"};

        try {
            int updated = contentResolver.update(Telephony.Sms.CONTENT_URI, values, selection, selectionArgs);
            Log.d("SmsHelper", "Marked " + updated + " messages as read");
        } catch (Exception e) {
            Log.e("SmsHelper", "Error marking messages as read", e);
        }
    }
}