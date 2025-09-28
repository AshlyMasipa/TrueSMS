package com.example.smsapp.models;

public class SmsMessage {
    private long threadId;
    private String address;
    private String contactName;
    private String body;
    private long timestamp;
    private boolean isSent;
    private boolean isRead;

    public SmsMessage(long threadId, String address, String contactName, String body,
                      long timestamp, boolean isSent, boolean isRead) {
        this.threadId = threadId;
        this.address = address;
        this.contactName = contactName;
        this.body = body;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.isRead = isRead;
    }

    // Getters
    public long getThreadId() { return threadId; }
    public String getAddress() { return address; }
    public String getContactName() { return contactName; }
    public String getBody() { return body; }
    public long getTimestamp() { return timestamp; }
    public boolean isSent() { return isSent; }
    public boolean isRead() { return isRead; }
}