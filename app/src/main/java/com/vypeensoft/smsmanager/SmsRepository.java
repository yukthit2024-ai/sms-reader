package com.vypeensoft.smsmanager;

import android.content.ContentValues;
import android.content.Context;
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
                Cursor cursor = contentResolver.query(uriSms, new String[]{"_id", "address", "body", "date", "read"}, null, null, "date DESC");

                if (cursor != null) {
                    int indexId = cursor.getColumnIndex("_id");
                    int indexAddress = cursor.getColumnIndex("address");
                    int indexBody = cursor.getColumnIndex("body");
                    int indexDate = cursor.getColumnIndex("date");
                    int indexRead = cursor.getColumnIndex("read");

                    while (cursor.moveToNext()) {
                        String id = cursor.getString(indexId);
                        String address = cursor.getString(indexAddress);
                        String body = cursor.getString(indexBody);
                        long dateMillis = cursor.getLong(indexDate);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.getDefault());
                        String timestamp = sdf.format(new Date(dateMillis));
                        boolean isRead = true; // default
                        if (indexRead != -1) {
                            isRead = cursor.getInt(indexRead) == 1;
                        }

                        smsList.add(new SmsModel(id, address, body, timestamp, isRead));
                    }
                    cursor.close();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            callback.onSmsLoaded(smsList);
        }).start();
    }

    public static void markSmsAsRead(Context context, String messageId) {
        new Thread(() -> {
            try {
                ContentValues values = new ContentValues();
                values.put("read", 1);
                Uri uriSms = Uri.parse("content://sms");
                context.getContentResolver().update(uriSms, values, "_id=" + messageId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
