package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "posts")
public class PostEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int    authorId;
    @NonNull public String authorName = "";
    public String authorPicPath = "";

    @NonNull public String description = "";
    public String mediaPath  = "";    // local image/video path
    public String mediaType  = "none"; // "image", "video", "none"
    public String intensity  = "general"; // urgent/important/promotion/announcement/general
    public String city       = "";
    public String location   = "";    // Specific area, bridge, etc.
    public int    likeCount  = 0;
    public int    commentCount = 0;
    public boolean isLiked   = false;
    public long   createdAt;
    public long   expiryTime = 0; // 0 means permanent
}
