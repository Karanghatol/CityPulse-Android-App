package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.citypulse.local.entity.PostEntity;
import java.util.List;

@Dao
public interface PostDao {
    @Insert
    long insert(PostEntity post);

    @Update
    void update(PostEntity post);

    @Delete
    void delete(PostEntity post);

    @Query("SELECT * FROM posts WHERE authorName = :user")
    List<PostEntity> getPostsByUser(String user);

    @Query("SELECT *, (SELECT COUNT(*) FROM post_likes WHERE postId = posts.id AND userId = :userId) > 0 AS isLiked " +
           "FROM posts WHERE (expiryTime = 0 OR expiryTime > :currentTime) " +
           "ORDER BY CASE WHEN intensity = 'urgent' THEN 1 ELSE 0 END ASC, likeCount DESC, createdAt DESC")
    LiveData<List<PostEntity>> getAllFeed(int userId, long currentTime);

    @Query("SELECT *, (SELECT COUNT(*) FROM post_likes WHERE postId = posts.id AND userId = :userId) > 0 AS isLiked " +
           "FROM posts WHERE city = :city AND (expiryTime = 0 OR expiryTime > :currentTime) " +
           "ORDER BY CASE WHEN intensity = 'urgent' THEN 1 ELSE 0 END ASC, likeCount DESC, createdAt DESC")
    LiveData<List<PostEntity>> getFeedByCity(int userId, String city, long currentTime);

    @Query("SELECT *, (SELECT COUNT(*) FROM post_likes WHERE postId = posts.id AND userId = :userId) > 0 AS isLiked " +
           "FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    LiveData<List<PostEntity>> getMyPosts(int userId);

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    PostEntity getById(int id);

    @Query("UPDATE posts SET isLiked = :liked, likeCount = likeCount + :delta WHERE id = :id")
    void toggleLike(int id, boolean liked, int delta);

    @Query("UPDATE posts SET likeCount = likeCount + :delta WHERE id = :id")
    void updateLikeCount(int id, int delta);

    @Query("DELETE FROM posts WHERE expiryTime != 0 AND expiryTime <= :currentTime")
    void deleteExpiredPosts(long currentTime);
}
