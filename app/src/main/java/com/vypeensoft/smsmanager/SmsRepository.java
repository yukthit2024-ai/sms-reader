package com.vypeensoft.smsmanager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.Instant;

public class SmsRepository {

    public interface SmsLoadCallback {
        void onSmsLoaded(List<SmsModel> smsList);
    }

    public static void getAllSms(ContentResolver contentResolver, SmsLoadCallback callback) {
        new Thread(() -> {
            List<SmsModel> smsList = new ArrayList<>();
            try {
                Uri uriSms = Uri.parse("content://sms/inbox");
                Cursor cursor = contentResolver.query(uriSms, new String[]{"address", "body", "date", "read"}, null, null, "date DESC");

                if (cursor != null) {
                    int indexAddress = cursor.getColumnIndex("address");
                    int indexBody = cursor.getColumnIndex("body");
                    int indexDate = cursor.getColumnIndex("date");
                    int indexRead = cursor.getColumnIndex("read");

                    while (cursor.moveToNext()) {
                        String address = cursor.getString(indexAddress);
                        String body = cursor.getString(indexBody);
                        long dateMillis = cursor.getLong(indexDate);
                        String timestamp = Instant.ofEpochMilli(dateMillis).toString();
                        boolean isRead = true; // default
                        if (indexRead != -1) {
                            isRead = cursor.getInt(indexRead) == 1;
                        }

                        smsList.add(new SmsModel(address, body, timestamp, isRead));
                    }
                    cursor.close();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            callback.onSmsLoaded(smsList);
        }).start();
    }
}
