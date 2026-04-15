package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "groups")
public class GroupEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
        public String createdBy;

    @NonNull public String name = "";
    public String reason   = "";   // hostel/mess/class/college/job/other
    public String city     = "";
    public int    adminId;
    public String adminName = "";
    public int    memberCount = 1;
    public String myStatus = "";   // pending/accepted/rejected/admin
    public long   createdAt;
}
