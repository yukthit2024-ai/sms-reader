package com.vypeensoft.smsmanager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmsRepository {

    public interface SmsLoadCallback {
        void onSmsLoaded(List<SmsModel> smsList);
    }

    public static void getAllSms(ContentResolver contentResolver, SmsLoadCallback callback) {
        new Thread(() -> {
            List<SmsModel> smsList = new ArrayList<>();
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor cursor = contentResolver.query(uriSms, new String[]{"address", "body", "date"}, null, null, "date DESC");

            if (cursor != null) {
                int indexAddress = cursor.getColumnIndex("address");
                int indexBody = cursor.getColumnIndex("body");
                int indexDate = cursor.getColumnIndex("date");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                while (cursor.moveToNext()) {
                    String address = cursor.getString(indexAddress);
                    String body = cursor.getString(indexBody);
                    long dateMillis = cursor.getLong(indexDate);
                    String timestamp = formatter.format(new Date(dateMillis));

                    smsList.add(new SmsModel(address, body, timestamp));
                }
                cursor.close();
            }

            callback.onSmsLoaded(smsList);
        }).start();
    }
}
