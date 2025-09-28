package com.example.smsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smsapp.R;
import com.example.smsapp.models.Conversation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationListAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.contactName.setText(conversation.getContactName());
        holder.lastMessage.setText(conversation.getLastMessage());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        holder.timestamp.setText(sdf.format(conversation.getTimestamp()));

        // Show unread count if any
        if (conversation.getMessageCount() > 0) {
            holder.unreadCount.setText(String.valueOf(conversation.getMessageCount()));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView lastMessage;
        public TextView timestamp;
        public TextView unreadCount;

        public ConversationViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.contact_name);
            lastMessage = view.findViewById(R.id.last_message);
            timestamp = view.findViewById(R.id.timestamp);
            unreadCount = view.findViewById(R.id.unread_count);
        }
    }
}