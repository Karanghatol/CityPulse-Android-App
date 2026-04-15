package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.citypulse.local.entity.CommentEntity;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    long insert(CommentEntity comment);

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt DESC")
    LiveData<List<CommentEntity>> getCommentsForPost(int postId);

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    int getCommentCountSync(int postId);
}
