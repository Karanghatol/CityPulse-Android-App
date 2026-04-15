package com.citypulse.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.JobEntity;
import com.citypulse.utils.SessionManager;

public class CreateJobActivity extends AppCompatActivity {

    private EditText etDesc, etCity, etPayment, etSkills, etJobType;
    private Button btnPost;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        sm = SessionManager.get();

        etDesc = findViewById(R.id.etJobDesc);
        etCity = findViewById(R.id.etCity);
        etPayment = findViewById(R.id.etPayment);
        etSkills = findViewById(R.id.etSkills);
        etJobType = findViewById(R.id.etJobType);
        btnPost = findViewById(R.id.btnPost);

        // Pre-fill city from session if available
        etCity.setText(sm.getUserCity());

        btnPost.setOnClickListener(v -> {

            String desc = etDesc.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String paymentStr = etPayment.getText().toString().trim();
            String skills = etSkills.getText().toString().trim();
            String jobType = etJobType.getText().toString().trim();

            if (desc.isEmpty() || city.isEmpty() || paymentStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            JobEntity job = new JobEntity();
            job.description = desc;
            job.city = city;
            job.payment = paymentStr;
            job.skills = skills;
            job.jobType = jobType;
            job.createdAt = System.currentTimeMillis();
            job.authorId = sm.getUserId();
            job.authorName = sm.getUserName();

            new Thread(() -> {
                AppDatabase.get(this).jobDao().insert(job);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Job posted!", Toast.LENGTH_SHORT).show();
                    finish(); // go back
                });
            }).start();
        });
    }
}
