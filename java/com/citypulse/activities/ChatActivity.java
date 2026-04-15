package com.citypulse.activities;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citypulse.R;
import com.citypulse.adapters.ChatAdapter;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.MessageEntity;
import com.citypulse.local.entity.NotificationEntity;
import com.citypulse.utils.SessionManager;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private RecyclerView rvChat;
    private AppDatabase db;
    private int jobId;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUser = SessionManager.get().getUserName();

        // ✅ initialize views
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);

        rvChat.setLayoutManager(new LinearLayoutManager(this));

        // ✅ database
        db = AppDatabase.get(this);

        // ✅ get job id
        jobId = getIntent().getIntExtra("jobId", -1);

        // ✅ load messages initially
        loadMessages();

        // ✅ send button
        btnSend.setOnClickListener(v -> {

            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            MessageEntity msg = new MessageEntity();
            msg.jobId = jobId;
            msg.sender = currentUser;
            msg.receiver = "Other";
            msg.message = text;
            msg.timestamp = System.currentTimeMillis();

            new Thread(() -> {

                // ✅ save message
                db.messageDao().insert(msg);

                // ✅ create notification
                NotificationEntity n = new NotificationEntity();
                n.title = "New Message";
                n.message = msg.sender + ": " + msg.message;
                n.timestamp = System.currentTimeMillis();
                n.isRead = false;
                n.type = "chat";
                n.jobId = jobId;

                db.notificationDao().insert(n);

                runOnUiThread(() -> {
                    etMessage.setText(""); // clear input
                    loadMessages(); // refresh chat
                });

            }).start();
        });
    }

    // ✅ load messages (outside onCreate)
    private void loadMessages() {
        new Thread(() -> {
            List<MessageEntity> list = db.messageDao().getMessages(jobId);

            runOnUiThread(() -> {
                ChatAdapter adapter = new ChatAdapter(list, currentUser);
                rvChat.setAdapter(adapter);

                if (!list.isEmpty()) {
                    rvChat.scrollToPosition(list.size() - 1);
                }
            });
        }).start();
    }
}
