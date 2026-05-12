package com.vypeensoft.smsmanager;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
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
            try {
                Uri uriSms = Uri.parse("content://sms/inbox");
                Cursor cursor = contentResolver.query(uriSms, new String[]{"_id", "address", "body", "date", "read"}, null, null, "date DESC");

                if (cursor != null) {
                    int indexId = cursor.getColumnIndex("_id");
                    int indexAddress = cursor.getColumnIndex("address");
                    int indexBody = cursor.getColumnIndex("body");
                    int indexDate = cursor.getColumnIndex("date");
                    int indexRead = cursor.getColumnIndex("read");

                    java.util.Map<String, String> contactCache = new java.util.HashMap<>();

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

                        String contactName;
                        if (contactCache.containsKey(address)) {
                            contactName = contactCache.get(address);
                        } else {
                            contactName = getContactName(contentResolver, address);
                            contactCache.put(address, contactName);
                        }
                        smsList.add(new SmsModel(id, address, contactName, body, timestamp, isRead));
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

    public static void deleteSms(Context context, String messageId, Runnable onSuccess) {
        new Thread(() -> {
            try {
                Uri uriSms = Uri.parse("content://sms/" + messageId);
                int rowsDeleted = context.getContentResolver().delete(uriSms, null, null);
                if (rowsDeleted > 0 && onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void exportMessages(Context context, List<SmsModel> smsList, String format, String exportPath, Runnable onSuccess) {
        new Thread(() -> {
            try {
                java.io.File dir = new java.io.File(exportPath);
                if (!dir.exists()) dir.mkdirs();

                String timeStamp = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "SMS-" + timeStamp + "." + format.toLowerCase();
                java.io.File file = new java.io.File(dir, fileName);

                java.io.FileWriter writer = new java.io.FileWriter(file);
                if ("xml".equalsIgnoreCase(format)) {
                    writer.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
                    writer.write("<smses count=\"" + smsList.size() + "\">\n");
                    for (SmsModel sms : smsList) {
                        writer.write("  <sms address=\"" + escapeXml(sms.getSender()) + 
                                     "\" body=\"" + escapeXml(sms.getBody()) + 
                                     "\" date=\"" + sms.getTimestamp() + 
                                     "\" read=\"" + (sms.isRead() ? "1" : "0") + "\" />\n");
                    }
                    writer.write("</smses>");
                } else if ("json".equalsIgnoreCase(format)) {
                    writer.write("[\n");
                    for (int i = 0; i < smsList.size(); i++) {
                        SmsModel sms = smsList.get(i);
                        writer.write("  {\n");
                        writer.write("    \"sender\": \"" + escapeJson(sms.getSender()) + "\",\n");
                        writer.write("    \"body\": \"" + escapeJson(sms.getBody()) + "\",\n");
                        writer.write("    \"timestamp\": \"" + sms.getTimestamp() + "\",\n");
                        writer.write("    \"read\": " + sms.isRead() + "\n");
                        writer.write("  }" + (i == smsList.size() - 1 ? "" : ",") + "\n");
                    }
                    writer.write("]");
                } else if ("csv".equalsIgnoreCase(format)) {
                    writer.write("Sender,Body,Timestamp,Read\n");
                    for (SmsModel sms : smsList) {
                        writer.write("\"" + escapeCsv(sms.getSender()) + "\",\"" + 
                                     escapeCsv(sms.getBody()) + "\",\"" + 
                                     sms.getTimestamp() + "\"," + 
                                     sms.isRead() + "\n");
                    }
                }
                writer.close();
                if (onSuccess != null) onSuccess.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String getContactName(ContentResolver contentResolver, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        
        try (Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
