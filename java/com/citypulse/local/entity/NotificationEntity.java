package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String message;
    public long timestamp;
    public boolean isRead;

    public String type; // "chat", "application", "group_request", "post_like", "post_comment", "job_accepted"
    public int jobId;
    public int groupId;
    public int postId;
    public String senderName;
}
