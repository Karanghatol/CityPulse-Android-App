package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "applications")
public class ApplicationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int jobId;
    public String applicantName;
    public long timestamp;
    public String status; // "pending", "accepted", "rejected"
}
