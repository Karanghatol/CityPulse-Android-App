package com.citypulse.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.citypulse.local.entity.MessageEntity;
import java.util.List;
import androidx.lifecycle.LiveData;

@Dao
public interface MessageDao {

    @Insert
    void insert(MessageEntity msg);

    @Update
    void update(MessageEntity msg);

    @Delete
    void delete(MessageEntity msg);

    @Query("SELECT * FROM messages WHERE jobId = :jobId ORDER BY timestamp ASC")
    List<MessageEntity> getMessages(int jobId);

    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getGroupMessages(int groupId);
}
