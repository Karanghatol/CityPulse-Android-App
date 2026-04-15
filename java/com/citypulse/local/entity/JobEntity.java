package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "jobs")
public class JobEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int    authorId;
    public String acceptedBy;
    @NonNull public String authorName = "";
    @NonNull public String jobType    = "";  // part-time/full-time/freelance/internship
    @NonNull public String description= "";
    public String payment      = "";
    public boolean isClosed; // job finished
    public String selectedUser; // final accepted person
    public String address      = "";
    public String city         = "";
    public String skills       = "";    // comma-separated
    public boolean travelAllowance = false;
    public boolean isVirtual   = false;
    public int    membersNeeded = 1;
    public String status       = "open"; // open/filled/closed
    public long   createdAt;
}
