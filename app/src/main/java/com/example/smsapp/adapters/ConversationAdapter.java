package com.example.smsapp.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smsapp.R;
import com.example.smsapp.models.SmsMessage;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MessageViewHolder> {

    private List<SmsMessage> messages;

    public ConversationAdapter(List<SmsMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
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

        // Clear previous backgrounds and layouts
        holder.messageText.setBackgroundResource(0);
        holder.messageLayout.setGravity(0);

        // Set background and alignment based on message type
        if (message.isSent()) {
            // Sent message - blue background, aligned to right
            holder.messageText.setBackgroundResource(R.drawable.sent_message_bg);
            holder.messageLayout.setGravity(android.view.Gravity.END);
            holder.messageText.setTextColor(Color.WHITE);
        } else {
            // Received message - green background, aligned to left
            holder.messageText.setBackgroundResource(R.drawable.received_message_bg);
            holder.messageLayout.setGravity(android.view.Gravity.START);
            holder.messageText.setTextColor(Color.WHITE);
        }

        Log.d("ConversationAdapter", "Message " + position + ": " +
                (message.isSent() ? "SENT" : "RECEIVED") + " - " + message.getBody());
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

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout messageLayout;
        public TextView messageText;

        public MessageViewHolder(View view) {
            super(view);
            messageLayout = view.findViewById(R.id.message_layout);
            messageText = view.findViewById(R.id.message_text);

            if (messageLayout == null) {
                Log.e("ConversationAdapter", "message_layout not found in layout");
            }
            if (messageText == null) {
                Log.e("ConversationAdapter", "message_text not found in layout");
            }
        }
    }
}