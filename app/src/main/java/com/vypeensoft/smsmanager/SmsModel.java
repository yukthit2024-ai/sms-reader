package com.vypeensoft.smsmanager;

import java.io.Serializable;

public class SmsModel implements Serializable {
    private String id;
    private String sender;
    private String contactName;
    private String body;
    private String timestamp;
    private boolean isRead;

    public SmsModel(String id, String sender, String contactName, String body, String timestamp, boolean isRead) {
        this.id = id;
        this.sender = sender;
        this.contactName = contactName;
        this.body = body;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getContactName() {
        return contactName;
    }

    public String getBody() {
        return body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }
}
