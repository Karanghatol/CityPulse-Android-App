package com.citypulse.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.citypulse.local.entity.PostLikeEntity;

@Dao
public interface PostLikeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PostLikeEntity like);

    @Delete
    void delete(PostLikeEntity like);

    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND userId = :userId)")
    boolean isLikedByUser(int postId, int userId);
}
