package com.vypeensoft.smsmanager;

import java.io.Serializable;

public class SmsModel implements Serializable {
    private String sender;
    private String body;
    private String timestamp;
    private boolean isRead;

    public SmsModel(String sender, String body, String timestamp, boolean isRead) {
        this.sender = sender;
        this.body = body;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSender() {
        return sender;
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
}
