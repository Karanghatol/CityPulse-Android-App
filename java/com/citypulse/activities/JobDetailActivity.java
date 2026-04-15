package com.citypulse.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.ApplicationEntity;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.local.entity.NotificationEntity;
import com.citypulse.utils.SessionManager;

public class JobDetailActivity extends AppCompatActivity {

    private TextView tvDesc, tvCity, tvPayment, tvStatus, tvJobType, tvAuthor, tvSkills, tvAddress;
    private Button btnAccept, btnChat;
    private AppDatabase db;
    private JobEntity job;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        sm = SessionManager.get();
        db = AppDatabase.get(this);

        setupToolbar();
        initViews();

        int jobId = getIntent().getIntExtra("jobId", -1);
        createNotificationChannel();

        // 🔹 Load job details
        new Thread(() -> {
            job = db.jobDao().getById(jobId);
            if (job != null) {
                runOnUiThread(() -> populateUI(job));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();

        // 🔹 Chat button
        btnChat.setOnClickListener(v -> {
            if (job != null) {
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra("jobId", job.id);
                startActivity(i);
            }
        });

        // 🔹 Apply button
        btnAccept.setOnClickListener(v -> handleApplication());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Job Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        tvJobType = findViewById(R.id.tvDetailJobType);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvPayment = findViewById(R.id.tvDetailPayment);
        tvCity = findViewById(R.id.tvDetailCity);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvSkills = findViewById(R.id.tvDetailSkills);
        tvAddress = findViewById(R.id.tvDetailAddress);
        btnAccept = findViewById(R.id.btnAccept);
        btnChat = findViewById(R.id.btnChat);
    }

    private void populateUI(JobEntity job) {
        tvJobType.setText(job.jobType);
        tvDesc.setText(job.description);
        tvPayment.setText("₹" + job.payment);
        tvCity.setText(job.city);
        tvAuthor.setText(job.authorName);
        
        boolean isOpen = "open".equals(job.status);
        tvStatus.setText(isOpen ? "OPEN" : "CLOSED");
        tvStatus.setTextColor(getResources().getColor(isOpen ? R.color.cp_promo : R.color.cp_urgent));

        if (job.skills != null && !job.skills.isEmpty()) {
            tvSkills.setText(job.skills);
        }
        
        if (job.address != null && !job.address.isEmpty()) {
            tvAddress.setText(job.address);
        }

        // ❗ UI adjustments based on user role
        if (job.authorId == sm.getUserId()) {
            btnAccept.setVisibility(View.GONE);
            btnChat.setText("View Applications");
            btnChat.setOnClickListener(v -> {
                Intent i = new Intent(this, ApplicationsActivity.class);
                i.putExtra("jobId", job.id);
                startActivity(i);
            });
        } else {
            // Check if already applied
            checkIfAlreadyApplied(job.id);
        }
    }

    private void checkIfAlreadyApplied(int jobId) {
        new Thread(() -> {
            // This is a simplified check. Ideally, ApplicationDao should have getApplication(jobId, applicantName)
            // For now, we'll just show the button and handle it in the click listener or add a query.
        }).start();
    }

    private void handleApplication() {
        if (job == null) return;

        if (!"open".equals(job.status)) {
            Toast.makeText(this, "Job is no longer accepting applications", Toast.LENGTH_SHORT).show();
            return;
        }

        String applicantName = sm.getUserName();

        ApplicationEntity app = new ApplicationEntity();
        app.jobId = job.id;
        app.applicantName = applicantName;
        app.timestamp = System.currentTimeMillis();
        app.status = "pending";

        NotificationEntity notification = new NotificationEntity();
        notification.title = "New Application";
        notification.message = applicantName + " applied for your job: " + job.description;
        notification.timestamp = System.currentTimeMillis();
        notification.isRead = false;
        notification.type = "application";
        notification.jobId = job.id;

        new Thread(() -> {
            db.applicationDao().insert(app);
            db.notificationDao().insert(notification);

            runOnUiThread(() -> {
                Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_LONG).show();
                btnAccept.setEnabled(false);
                btnAccept.setText("Applied");
                showSystemNotification(applicantName);
            });
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(
                        "job_channel",
                        "Job Updates",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                nm.createNotificationChannel(channel);
            }
        }
    }

    private void showSystemNotification(String applicantName) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "job_channel")
                .setContentTitle("Application Sent")
                .setContentText("You have successfully applied for the job.")
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
