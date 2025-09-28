package com.example.smsapp.adapters;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smsapp.R;
import com.example.smsapp.models.SmsMessage;
import com.example.smsapp.utils.PhishingDetector;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MessageViewHolder> {

    private List<SmsMessage> messages;
    private OnPhishingRecheckListener recheckListener;

    public interface OnPhishingRecheckListener {
        void onPhishingRecheck(SmsMessage message, int position);
    }

    public ConversationAdapter(List<SmsMessage> messages, OnPhishingRecheckListener recheckListener) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.recheckListener = recheckListener;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        SmsMessage message = messages.get(position);
        holder.messageText.setText(message.getBody());

        // Enable clickable links
        holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());

        // Manually linkify the text (as backup)
        Linkify.addLinks(holder.messageText, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);

        // Clear previous backgrounds
        holder.messageLayout.setBackgroundColor(Color.TRANSPARENT);

        // Show phishing warning if detected
        if (message.isPhishing()) {
            holder.phishingWarning.setVisibility(View.VISIBLE);
            String confidencePercent = String.format("%.0f%%", message.getPhishingConfidence() * 100);
            holder.phishingWarning.setText("⚠️ Potential Phishing (" + confidencePercent + " confidence)");

            // Set phishing background (light red)
            holder.messageLayout.setBackgroundColor(Color.parseColor("#FFF5F5"));
        } else {
            holder.phishingWarning.setVisibility(View.GONE);
        }

        // Show recheck button for all received messages (not sent ones)
        if (!message.isSent()) {
            holder.recheckButton.setVisibility(View.VISIBLE);
            holder.recheckButton.setOnClickListener(v -> {
                if (recheckListener != null) {
                    recheckListener.onPhishingRecheck(message, position);
                }
            });
        } else {
            holder.recheckButton.setVisibility(View.GONE);
        }

        // Set background and alignment based on message type
        if (message.isSent()) {
            // Sent message - blue background, aligned to right
            holder.messageText.setBackgroundResource(R.drawable.sent_message_bg);
            holder.messageContainer.setGravity(android.view.Gravity.END);
            holder.messageText.setTextColor(Color.WHITE);
        } else {
            // Received message - green background, aligned to left
            holder.messageText.setBackgroundResource(R.drawable.received_message_bg);
            holder.messageContainer.setGravity(android.view.Gravity.START);
            holder.messageText.setTextColor(Color.WHITE);
        }

        Log.d("ConversationAdapter", "Message " + position + ": " +
                (message.isSent() ? "SENT" : "RECEIVED") +
                (message.isPhishing() ? " PHISHING (" + message.getPhishingConfidence() + ")" : " SAFE") +
                " - " + message.getBody());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<SmsMessage> newMessages) {
        this.messages = newMessages != null ? newMessages : new ArrayList<>();
        notifyDataSetChanged();
        Log.d("ConversationAdapter", "Updated messages: " + this.messages.size());
    }

    public void updateMessage(int position, SmsMessage message) {
        if (position >= 0 && position < messages.size()) {
            messages.set(position, message);
            notifyItemChanged(position);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout messageLayout;
        public LinearLayout messageContainer;
        public TextView messageText;
        public TextView phishingWarning;
        public ImageButton recheckButton;

        public MessageViewHolder(View view) {
            super(view);
            messageLayout = view.findViewById(R.id.message_layout);
            messageContainer = view.findViewById(R.id.message_container);
            messageText = view.findViewById(R.id.message_text);
            phishingWarning = view.findViewById(R.id.phishing_warning);
            recheckButton = view.findViewById(R.id.recheck_button);

            // Ensure text view can handle links
            if (messageText != null) {
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
                messageText.setLinksClickable(true);
            }

            // Initialize phishing warning (hidden by default)
            if (phishingWarning != null) {
                phishingWarning.setVisibility(View.GONE);
            }

            // Initialize recheck button (hidden by default)
            if (recheckButton != null) {
                recheckButton.setVisibility(View.GONE);
            }
        }
    }
}