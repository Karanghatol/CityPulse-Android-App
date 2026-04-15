package com.citypulse.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.NotificationEntity;
import com.citypulse.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView rv;
    private NotifAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotifAdapter();
        rv.setAdapter(adapter);

        db = AppDatabase.get(this);

        loadNotifications();
        new Thread(() -> db.notificationDao().markAllRead()).start();
    }

    private void loadNotifications() {
        db.notificationDao().getAllLiveData().observe(this, list -> {
            adapter.setList(list);
        });
    }

    class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        private List<NotificationEntity> list = new ArrayList<>();

        void setList(List<NotificationEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            NotificationEntity n = list.get(position);
            holder.tvTitle.setText(n.title);
            holder.tvMsg.setText(n.message);
            holder.tvTime.setText(TimeUtils.getRelativeTime(n.timestamp));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = null;
                switch (n.type != null ? n.type : "") {
                    case "chat":
                    case "group_request":
                        intent = new Intent(NotificationActivity.this, GroupChatActivity.class);
                        intent.putExtra("groupId", n.groupId);
                        break;
                    case "application":
                    case "job_accepted":
                        intent = new Intent(NotificationActivity.this, JobDetailActivity.class);
                        intent.putExtra("jobId", n.jobId);
                        break;
                    case "post_like":
                    case "post_comment":
                        // Ideally, we'd have a PostDetailActivity. For now, we can go to MainActivity/Feed
                        intent = new Intent(NotificationActivity.this, MainActivity.class);
                        break;
                }
                if (intent != null) startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMsg, tvTime;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvNotifTitle);
                tvMsg = v.findViewById(R.id.tvNotifMsg);
                tvTime = v.findViewById(R.id.tvNotifTime);
            }
        }
    }
}
