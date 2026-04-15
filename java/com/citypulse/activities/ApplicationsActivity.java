package com.citypulse.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.ApplicationEntity;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.local.entity.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationsActivity extends AppCompatActivity {

    private RecyclerView rvApps;
    private AppsAdapter adapter;
    private AppDatabase db;
    private int jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        db = AppDatabase.get(this);
        jobId = getIntent().getIntExtra("jobId", -1);

        rvApps = findViewById(R.id.rvApplications);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppsAdapter();
        rvApps.setAdapter(adapter);

        loadApplications();
    }

    private void loadApplications() {
        new Thread(() -> {
            List<ApplicationEntity> list = db.applicationDao().getApplications(jobId);
            runOnUiThread(() -> adapter.setList(list));
        }).start();
    }

    class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.VH> {
        private List<ApplicationEntity> list = new ArrayList<>();

        void setList(List<ApplicationEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ApplicationEntity app = list.get(position);
            holder.tvName.setText(app.applicantName);
            holder.tvStatus.setText(app.status);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(app.timestamp)));

            if ("accepted".equals(app.status)) {
                holder.tvStatus.setTextColor(getResources().getColor(R.color.cp_promo));
                holder.btnAccept.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setTextColor(getResources().getColor(R.color.cp_warning));
                holder.btnAccept.setVisibility(View.VISIBLE);
            }

            holder.btnAccept.setOnClickListener(v -> acceptApp(app));
            holder.btnChat.setOnClickListener(v -> {
                Intent i = new Intent(ApplicationsActivity.this, ChatActivity.class);
                i.putExtra("receiverName", app.applicantName);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvTime, tvStatus;
            View btnAccept, btnChat;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvApplicantName);
                tvTime = v.findViewById(R.id.tvApplyTime);
                tvStatus = v.findViewById(R.id.tvAppStatus);
                btnAccept = v.findViewById(R.id.btnAcceptApplicant);
                btnChat = v.findViewById(R.id.btnChatApplicant);
            }
        }
    }

    private void acceptApp(ApplicationEntity app) {
        new Thread(() -> {
            JobEntity job = db.jobDao().getById(jobId);
            if (job != null) {
                app.status = "accepted";
                job.status = "filled"; // Mark job as filled
                job.selectedUser = app.applicantName;

                db.applicationDao().update(app);
                db.jobDao().update(job);

                // Notify applicant
                NotificationEntity n = new NotificationEntity();
                n.title = "Application Accepted!";
                n.message = "Your application for '" + job.description + "' has been accepted by " + job.authorName;
                n.timestamp = System.currentTimeMillis();
                n.type = "application_result";
                db.notificationDao().insert(n);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Application accepted!", Toast.LENGTH_SHORT).show();
                    loadApplications();
                });
            }
        }).start();
    }
}
