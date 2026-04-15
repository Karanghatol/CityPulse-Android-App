package com.citypulse.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.citypulse.local.entity.ApplicationEntity;
import java.util.List;

@Dao
public interface ApplicationDao {

    @Insert
    void insert(ApplicationEntity app);

    @Query("SELECT * FROM applications WHERE jobId = :jobId")
    List<ApplicationEntity> getApplications(int jobId);

    @Update
    void update(ApplicationEntity app);

    @Query("SELECT COUNT(*) FROM applications WHERE jobId = :jobId")
    int getApplicationCount(int jobId);

    @Query("SELECT * FROM applications WHERE jobId = :jobId AND applicantName = :name LIMIT 1")
    ApplicationEntity getByUserAndJob(int jobId, String name);

    @Delete
    void delete(ApplicationEntity app);
}
