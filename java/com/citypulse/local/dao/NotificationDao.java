package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.citypulse.local.entity.NotificationEntity;
import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(NotificationEntity n);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    List<NotificationEntity> getAll();

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getAllLiveData();

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadCount();

    @Query("UPDATE notifications SET isRead = 1")
    void markAllRead();
}
