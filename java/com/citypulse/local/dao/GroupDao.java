package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.citypulse.local.entity.GroupEntity;
import java.util.List;

@Dao
public interface GroupDao {
    @Insert
    long insert(GroupEntity group);

    @Update
    void update(GroupEntity group);

    @Query("SELECT * FROM groups WHERE createdBy = :user OR myStatus = 'accepted' OR myStatus = 'admin' ORDER BY createdAt DESC")
    LiveData<List<GroupEntity>> getMyActivityGroups(String user);

    @Query("SELECT * FROM groups WHERE createdBy = :user OR myStatus = 'accepted' OR myStatus = 'admin' ORDER BY createdAt DESC")
    List<GroupEntity> getMyGroups(String user);

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    LiveData<List<GroupEntity>> getAllGroups();

    @Query("SELECT * FROM groups WHERE city = :city ORDER BY createdAt DESC")
    LiveData<List<GroupEntity>> getGroupsByCity(String city);

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    GroupEntity getById(int id);
}
