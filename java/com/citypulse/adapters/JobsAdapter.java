package com.citypulse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.citypulse.R;
import com.citypulse.activities.JobDetailActivity;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.ApplicationEntity;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.utils.SessionManager;

import java.util.List;

public class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.ViewHolder> {

    private Context context;
    private List<JobEntity> jobList;

    public JobsAdapter(Context context, List<JobEntity> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    public void updateData(List<JobEntity> newData) {
        this.jobList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JobEntity job = jobList.get(position);
        int currentUserId = SessionManager.get().getUserId();
        String currentUserName = SessionManager.get().getUserName();

        new Thread(() -> {
            int count = AppDatabase.get(context).applicationDao().getApplicationCount(job.id);
            ApplicationEntity existingApp = AppDatabase.get(context).applicationDao().getByUserAndJob(job.id, currentUserName);

            ((Activity) context).runOnUiThread(() -> {
                holder.tvDesc.setText(job.description + "\nApplicants: " + count);
                if (job.acceptedBy != null) {
                    holder.tvDesc.append("\nAccepted by: " + job.acceptedBy);
                }

                updateButtonState(holder, job, existingApp, currentUserId, currentUserName);
            });
        }).start();

        // ✅ Author
        holder.tvAuthor.setText(job.authorName != null && !job.authorName.isEmpty() ? job.authorName : "Unknown");
        if (job.authorId == currentUserId) {
            holder.tvAuthor.setText("You");
        }

        // ✅ Time
        long diff = System.currentTimeMillis() - job.createdAt;
        long minutes = diff / 60000;
        holder.tvTime.setText(minutes == 0 ? "Just now" : minutes + " min ago");

        // ✅ Other fields
        holder.tvCity.setText(job.city);
        holder.tvPayment.setText("₹" + job.payment);
        
        if (job.skills != null && !job.skills.isEmpty()) {
            holder.tvSkills.setVisibility(View.VISIBLE);
            holder.tvSkills.setText("Skills: " + job.skills);
        } else {
            holder.tvSkills.setVisibility(View.GONE);
        }

        // ✅ Click → open detail screen or applications
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (job.authorId == currentUserId) {
                intent = new Intent(context, com.citypulse.activities.ApplicationsActivity.class);
            } else {
                intent = new Intent(context, JobDetailActivity.class);
            }
            intent.putExtra("jobId", job.id);
            context.startActivity(intent);
        });
    }

    private void updateButtonState(ViewHolder holder, JobEntity job, ApplicationEntity existingApp, int currentUserId, String currentUserName) {
        if (job.authorId == currentUserId) {
            holder.btnAccept.setVisibility(View.GONE);
        } else if (job.acceptedBy != null) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setText("Job Closed");
            holder.btnAccept.setEnabled(false);
            holder.btnAccept.setAlpha(0.5f);
        } else if (existingApp != null) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setText("Withdraw");
            holder.btnAccept.setEnabled(true);
            holder.btnAccept.setAlpha(1.0f);
            holder.btnAccept.setOnClickListener(v -> withdraw(holder, job, existingApp, currentUserId, currentUserName));
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setText("Apply for Job");
            holder.btnAccept.setEnabled(true);
            holder.btnAccept.setAlpha(1.0f);
            holder.btnAccept.setOnClickListener(v -> apply(holder, job, currentUserId, currentUserName));
        }
    }

    private void apply(ViewHolder holder, JobEntity job, int currentUserId, String currentUserName) {
        new Thread(() -> {
            ApplicationEntity app = new ApplicationEntity();
            app.jobId = job.id;
            app.applicantName = currentUserName;
            app.timestamp = System.currentTimeMillis();
            app.status = "pending";

            AppDatabase.get(context).applicationDao().insert(app);
            ApplicationEntity newApp = AppDatabase.get(context).applicationDao().getByUserAndJob(job.id, currentUserName);

            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "Applied successfully!", Toast.LENGTH_SHORT).show();
                updateButtonState(holder, job, newApp, currentUserId, currentUserName);
            });
        }).start();
    }

    private void withdraw(ViewHolder holder, JobEntity job, ApplicationEntity existingApp, int currentUserId, String currentUserName) {
        new Thread(() -> {
            AppDatabase.get(context).applicationDao().delete(existingApp);

            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "Application withdrawn", Toast.LENGTH_SHORT).show();
                updateButtonState(holder, job, null, currentUserId, currentUserName);
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAuthor, tvTime, tvDesc, tvCity, tvPayment, tvSkills;
        Button btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAuthor = itemView.findViewById(R.id.tvJobAuthor);
            tvTime = itemView.findViewById(R.id.tvJobTime);
            tvDesc = itemView.findViewById(R.id.tvJobDesc);
            tvCity = itemView.findViewById(R.id.tvJobCity);
            tvPayment = itemView.findViewById(R.id.tvJobPayment);
            tvSkills = itemView.findViewById(R.id.tvJobSkills);
            btnAccept = itemView.findViewById(R.id.btnAcceptJob);
        }
    }
}
