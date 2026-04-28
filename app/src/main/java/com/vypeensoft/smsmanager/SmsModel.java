package com.vypeensoft.smsmanager;

import java.io.Serializable;

public class SmsModel implements Serializable {
    private String sender;
    private String body;
    private String timestamp;

    public SmsModel(String sender, String body, String timestamp) {
        this.sender = sender;
        this.body = body;
        this.timestamp = timestamp;
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
}
