package com.example.smsapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smsapp.adapters.ConversationAdapter;
import com.example.smsapp.adapters.ConversationListAdapter;
import com.example.smsapp.dialogs.ContactPickerDialog;
import com.example.smsapp.models.Conversation;
import com.example.smsapp.models.SmsMessage;
import com.example.smsapp.utils.ContactUtils;
import com.example.smsapp.utils.SmsHelper;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ContactPickerDialog.ContactSelectedListener {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private RecyclerView conversationRecyclerView;
    private RecyclerView conversationsListRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton contactPickerButton;
    private TextView currentContactView;
    private View conversationView;
    private View conversationsListView;

    // Adapters
    private ConversationAdapter conversationAdapter;
    private ConversationListAdapter conversationsListAdapter;

    // Data
    private List<SmsMessage> currentMessages;
    private List<Conversation> allConversations;
    private Conversation currentConversation;


    private void setupRefreshButton() {
        ImageButton refreshButton = findViewById(R.id.refresh_button);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                refreshConversations();
                if (currentConversation != null) {
                    refreshCurrentConversation();
                }
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";

        // Remove all non-digit characters except +
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");

        // If it starts with +27, convert to 0 format for South Africa
        if (normalized.startsWith("+27") && normalized.length() == 11) {
            return "0" + normalized.substring(3);
        }

        // If it starts with 27, convert to 0 format
        if (normalized.startsWith("27") && normalized.length() == 10) {
            return "0" + normalized.substring(2);
        }

        return normalized;
    }

    // Broadcast receiver for new messages
    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("NEW_SMS_RECEIVED".equals(intent.getAction())) {
                String sender = intent.getStringExtra("sender");
                String message = intent.getStringExtra("message");

                // Normalize both numbers before comparison
                String normalizedSender = normalizePhoneNumber(sender);
                String normalizedCurrent = currentConversation != null ?
                        normalizePhoneNumber(currentConversation.getPhoneNumber()) : "";

                if (currentConversation != null && normalizedSender.equals(normalizedCurrent)) {
                    // Add to current conversation
                    SmsMessage newMessage = new SmsMessage(
                            currentConversation.getThreadId(), sender,
                            currentConversation.getContactName(), message,
                            System.currentTimeMillis(), false, true
                    );

                    currentMessages.add(newMessage);
                    conversationAdapter.updateMessages(currentMessages);
                    conversationRecyclerView.scrollToPosition(currentMessages.size() - 1);

                    Log.d("SMS_FLOW", "Message added to current conversation");
                    Toast.makeText(MainActivity.this, "New message received!", Toast.LENGTH_SHORT).show();
                }

                refreshConversations();
            }
        }
    };
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("NEW_SMS_RECEIVED");
        filter.addAction("REFRESH_CONVERSATIONS");
        registerReceiver(smsReceiver, filter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_conversations);

        initializeViews();
        checkPermissions();
        registerReceivers();
        setupRefreshButton();
    }

    private void initializeViews() {
        conversationsListRecyclerView = findViewById(R.id.conversations_list_view);
        conversationRecyclerView = findViewById(R.id.conversation_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        contactPickerButton = findViewById(R.id.contact_picker_button);
        currentContactView = findViewById(R.id.current_contact);
        conversationView = findViewById(R.id.conversation_view);
        conversationsListView = findViewById(R.id.conversations_list_view);

        // Setup conversations list
        allConversations = new ArrayList<>();
        conversationsListAdapter = new ConversationListAdapter(allConversations, this::onConversationSelected);
        conversationsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsListRecyclerView.setAdapter(conversationsListAdapter);

        // Setup message conversation
        currentMessages = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(currentMessages);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationRecyclerView.setAdapter(conversationAdapter);

        sendButton.setOnClickListener(v -> sendMessage());
        contactPickerButton.setOnClickListener(v -> showContactPicker());

    }



    // ADD THIS METHOD - It was missing!
    private void showContactPicker() {
        // Check if we have contact permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);
            return;
        }

        ContactPickerDialog dialog = ContactPickerDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "contact_picker");
    }



    private void refreshConversations() {
        Log.d("MainActivity", "Refreshing conversations...");

        new Thread(() -> {
            try {
                List<Conversation> conversations = SmsHelper.getAllConversations(MainActivity.this);
                Log.d("MainActivity", "Retrieved " + conversations.size() + " conversations from database");

                runOnUiThread(() -> {
                    allConversations.clear();
                    allConversations.addAll(conversations);
                    conversationsListAdapter.updateConversations(allConversations);

                    Log.d("MainActivity", "UI updated with " + allConversations.size() + " conversations");

                    // If we have a current conversation, make sure it's still in the list
                    if (currentConversation != null) {
                        boolean found = false;
                        for (Conversation conv : allConversations) {
                            if (conv.getPhoneNumber().equals(currentConversation.getPhoneNumber())) {
                                currentConversation = conv; // Update with latest data
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Log.d("MainActivity", "Current conversation no longer in list");
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Error refreshing conversations", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    private void refreshCurrentConversation() {
        if (currentConversation != null) {
            new Thread(() -> {
                List<SmsMessage> messages = SmsHelper.getMessagesForThread(
                        MainActivity.this, currentConversation.getThreadId());

                new Handler(Looper.getMainLooper()).post(() -> {
                    // Use updateMessages instead of clearing and notifying separately
                    conversationAdapter.updateMessages(messages);
                    if (!currentMessages.isEmpty()) {
                        conversationRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
            }).start();
        }
    }

    private void onConversationSelected(Conversation conversation) {
        currentConversation = conversation;
        currentContactView.setText(conversation.getContactName());
        showConversationView();
        refreshCurrentConversation();

        // Mark messages as read
        SmsHelper.markMessageAsRead(this, conversation.getThreadId());
    }

    @Override
    public void onContactSelected(com.example.smsapp.models.Contact contact) {
        // Check if conversation already exists
        Conversation existingConversation = findConversationByNumber(contact.getPhoneNumber());

        if (existingConversation != null) {
            onConversationSelected(existingConversation);
        } else {
            // Create new conversation view for new contact
            currentContactView.setText(contact.getName());
            showConversationView();
            currentMessages.clear();
            conversationAdapter.updateMessages(currentMessages);
            // Store the contact for sending messages
            currentConversation = new Conversation(-1, contact.getName(), contact.getPhoneNumber(),
                    "", System.currentTimeMillis(), 0);
        }
    }

    private Conversation findConversationByNumber(String phoneNumber) {
        for (Conversation conversation : allConversations) {
            if (conversation.getPhoneNumber().equals(phoneNumber)) {
                return conversation;
            }
        }
        return null;
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty() && currentConversation != null) {
            if (hasSmsPermission()) {
                // Clear input immediately
                messageEditText.setText("");

                // Create a temporary message (will be updated when send completes)
                SmsMessage tempMessage = new SmsMessage(
                        currentConversation.getThreadId(),
                        currentConversation.getPhoneNumber(),
                        currentConversation.getContactName(),
                        messageText,
                        System.currentTimeMillis(),
                        true,  // isSent
                        true   // isRead
                );

                // Add to UI immediately (optimistic update)
                currentMessages.add(tempMessage);
                conversationAdapter.updateMessages(currentMessages);
                conversationRecyclerView.scrollToPosition(currentMessages.size() - 1);

                // Send SMS using system method with callback
                SmsSender.sendSms(this, currentConversation.getPhoneNumber(), messageText,
                        new SmsSender.SmsSendCallback() {
                            @Override
                            public void onSending() {
                                Log.d("MainActivity", "SMS sending initiated");
                                // Don't show toast yet - wait for result
                            }

                            @Override
                            public void onSendSuccess() {
                                Log.d("MainActivity", "SMS sent successfully");
                                // The receiver will handle the database update and toast
                            }

                            @Override
                            public void onSendFailed(String error) {
                                Log.e("MainActivity", "SMS send failed: " + error);
                                runOnUiThread(() -> {
                                    // Remove the optimistic message if send failed
                                    currentMessages.remove(tempMessage);
                                    conversationAdapter.updateMessages(currentMessages);
                                    Toast.makeText(MainActivity.this,
                                            "Failed to send: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });

            } else {
                checkPermissions();
            }
        }
    }

    private void showConversationView() {
        conversationsListView.setVisibility(View.GONE);
        conversationView.setVisibility(View.VISIBLE);
    }

    private void showConversationsListView() {
        conversationView.setVisibility(View.GONE);
        conversationsListView.setVisibility(View.VISIBLE);
        currentConversation = null;
        currentContactView.setText("Conversations");
    }







    @Override
    public void onBackPressed() {
        if (conversationView.getVisibility() == View.VISIBLE) {
            showConversationsListView();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS
        };

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPermissions()) {
                refreshConversations();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }
}