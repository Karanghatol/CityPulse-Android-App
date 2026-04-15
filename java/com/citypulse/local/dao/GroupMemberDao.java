package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.citypulse.local.entity.GroupMemberEntity;
import java.util.List;

@Dao
public interface GroupMemberDao {
    @Insert
    void insert(GroupMemberEntity member);

    @Update
    void update(GroupMemberEntity member);

    @Delete
    void delete(GroupMemberEntity member);

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND status = 'accepted'")
    LiveData<List<GroupMemberEntity>> getAcceptedMembers(int groupId);

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND status = 'pending'")
    LiveData<List<GroupMemberEntity>> getPendingRequests(int groupId);

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId LIMIT 1")
    GroupMemberEntity getMember(int groupId, int userId);

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId LIMIT 1")
    LiveData<GroupMemberEntity> getMemberLiveData(int groupId, int userId);
}
