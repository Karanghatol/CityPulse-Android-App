package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_members")
public class GroupMemberEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int groupId;
    public int userId;
    public String userName;
    public String userRole; // "admin", "member"
    public String status;   // "pending", "accepted"
    public long joinedAt;
}
