package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "comments")
public class CommentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int postId;
    public int authorId;
    @NonNull public String authorName = "";
    public String authorPicPath = "";
    @NonNull public String content = "";
    public long createdAt;
}
