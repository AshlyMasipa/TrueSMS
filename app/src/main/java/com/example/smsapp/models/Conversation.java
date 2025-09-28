package com.example.smsapp.models;

public class Conversation {
    private long threadId;
    private String contactName;
    private String phoneNumber;
    private String lastMessage;
    private long timestamp;
    private int messageCount;

    public Conversation(long threadId, String contactName, String phoneNumber,
                        String lastMessage, long timestamp, int messageCount) {
        this.threadId = threadId;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.messageCount = messageCount;
    }

    // Getters
    public long getThreadId() { return threadId; }
    public String getContactName() { return contactName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public int getMessageCount() { return messageCount; }

    // Add setters
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
}