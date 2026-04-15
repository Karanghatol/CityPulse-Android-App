package com.citypulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.MessageEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private List<MessageEntity> messages = new ArrayList<>();
    private String currentUser;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(String currentUser) {
        this.currentUser = currentUser;
    }

    public ChatAdapter(List<MessageEntity> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).sender != null && messages.get(position).sender.equals(currentUser)) {
            return 1; // sent
        } else {
            return 0; // received
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.item_chat_sent : R.layout.item_chat_received;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageEntity msg = messages.get(position);
        holder.tvMessage.setText(msg.message);
        if (holder.tvSender != null) {
            holder.tvSender.setText(msg.sender);
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText(timeFormat.format(new Date(msg.timestamp)));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (msg.sender != null && msg.sender.equals(currentUser)) {
                showEditDeleteDialog(v, msg, position);
                return true;
            }
            return false;
        });
    }

    private void showEditDeleteDialog(View v, MessageEntity msg, int position) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(v.getContext())
                .setTitle("Message Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(v, msg, position);
                    } else {
                        deleteMessage(v, msg, position);
                    }
                }).show();
    }

    private void showEditDialog(View v, MessageEntity msg, int position) {
        android.widget.EditText et = new android.widget.EditText(v.getContext());
        et.setText(msg.message);
        new AlertDialog.Builder(v.getContext())
                .setTitle("Edit Message")
                .setView(et)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = et.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        msg.message = newText;
                        new Thread(() -> {
                            AppDatabase.get(v.getContext()).messageDao().update(msg);
                            ((android.app.Activity)v.getContext()).runOnUiThread(() -> notifyItemChanged(position));
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(View v, MessageEntity msg, int position) {
        new Thread(() -> {
            AppDatabase.get(v.getContext()).messageDao().delete(msg);
            // Note: If using LiveData, the observer in Activity will handle refresh.
            // But we can also manually update the list for immediate feedback if not using LiveData.
        }).start();
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvSender, tvTime;

        VH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvSender = v.findViewById(R.id.tvSender);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
