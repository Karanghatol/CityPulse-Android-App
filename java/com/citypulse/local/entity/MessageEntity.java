package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int jobId; // Use -1 or 0 for non-job chats
    public int groupId; // Added for group chat
    public String sender;
    public String receiver;
    public String message;
    public long timestamp;
}
