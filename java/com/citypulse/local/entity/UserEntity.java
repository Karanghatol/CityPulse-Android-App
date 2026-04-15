package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull public String name  = "";
    @NonNull public String email = "";
    @NonNull public String passwordHash = "";
    public String phone        = "";
    public String city         = "";
    public String profilePicPath = "";   // local file path
    public boolean interestedInJobs = false;
    public long createdAt;
}
