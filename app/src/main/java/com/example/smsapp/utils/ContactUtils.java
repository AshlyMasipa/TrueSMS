package com.example.smsapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.example.smsapp.models.Contact;
import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    public static List<Contact> getAllContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(
                            ContactsContract.Contacts.DISPLAY_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // Clean phone number
                    if (phone != null) {
                        phone = phone.replaceAll("[^0-9+]", "");
                        contacts.add(new Contact(name, phone));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ContactUtils", "Error getting contacts", e);
        }

        return contacts;
    }

    public static String getContactName(Context context, String phoneNumber) {
        if (phoneNumber == null) return "Unknown";

        ContentResolver contentResolver = context.getContentResolver();
        String contactName = phoneNumber; // Default to phone number if not found

        // Clean the phone number for comparison
        String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");

        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME
        };

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber));

        try (Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e("ContactUtils", "Error getting contact name for: " + phoneNumber, e);
        }

        return contactName;
    }
}