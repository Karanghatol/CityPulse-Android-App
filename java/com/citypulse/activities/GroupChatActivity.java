package com.citypulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.citypulse.R;
import com.citypulse.adapters.ChatAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.MessageEntity;
import com.citypulse.utils.SessionManager;
import java.util.concurrent.Executors;

public class GroupChatActivity extends AppCompatActivity {

    private int groupId;
    private String groupName;
    private AppDatabase db;
    private SessionManager sm;
    private ChatAdapter adapter;
    private RecyclerView rvChat;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        db = AppDatabase.get(this);
        sm = SessionManager.get();

        groupId = getIntent().getIntExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");

        TextView tvName = findViewById(R.id.tvToolbarGroupName);
        tvName.setText(groupName);

        findViewById(R.id.layoutGroupHeader).setOnClickListener(v -> {
            Intent intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        rvChat = findViewById(R.id.rvGroupChat);
        etMessage = findViewById(R.id.etGroupMessage);
        ImageButton btnSend = findViewById(R.id.btnSendGroupMessage);

        adapter = new ChatAdapter(sm.getUserName());
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        db.messageDao().getGroupMessages(groupId).observe(this, messages -> {
            adapter.setMessages(messages);
            if (!messages.isEmpty()) {
                rvChat.scrollToPosition(messages.size() - 1);
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            MessageEntity msg = new MessageEntity();
            msg.groupId = groupId;
            msg.jobId = 0;
            msg.sender = sm.getUserName();
            msg.message = text;
            msg.timestamp = System.currentTimeMillis();

            Executors.newSingleThreadExecutor().execute(() -> {
                db.messageDao().insert(msg);
                runOnUiThread(() -> etMessage.setText(""));
            });
        });
    }
}
