package com.example.smsapp.models;

public class SmsMessage {
    private long threadId;
    private String address;
    private String contactName; // Add this field

    private String body;
    private long timestamp;
    private boolean isSent;
    private boolean isRead;
    private boolean isPhishing;
    private double phishingConfidence;
    private int type; // Add this field to determine sent/received

    // Constructors
    public SmsMessage() {}

    public SmsMessage(long threadId, String address,String contactName, String body, long timestamp, boolean isSent,boolean isRead) {
        this.threadId = threadId;
        this.address = address;
        this.contactName = contactName;
        this.body = body;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.isRead = isRead;
        this.isPhishing = false;
        this.phishingConfidence = 0.0;
        this.type = 2; // Default to received (2)
    }


    // Getters and Setters
    public long getThreadId() { return threadId; }


    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() {
        return contactName;
    }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSent() {
        return type == 1; // 1 for sent, 2 for received
    }
    public boolean isRead() { return isRead; }
    public boolean isPhishing() { return isPhishing; }
    public void setPhishing(boolean phishing) { isPhishing = phishing; }

    public double getPhishingConfidence() { return phishingConfidence; }
    public void setPhishingConfidence(double phishingConfidence) {
        this.phishingConfidence = phishingConfidence;
    }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    // Method for ConversationAdapter

}