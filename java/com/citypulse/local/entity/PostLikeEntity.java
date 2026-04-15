package com.citypulse.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "post_likes",
    primaryKeys = {"postId", "userId"},
    foreignKeys = {
        @ForeignKey(entity = PostEntity.class, parentColumns = "id", childColumns = "postId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("postId"), @Index("userId")}
)
public class PostLikeEntity {
    public int postId;
    public int userId;

    public PostLikeEntity(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
