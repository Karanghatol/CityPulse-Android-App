package com.citypulse.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.citypulse.local.entity.JobEntity;
import java.util.List;

@Dao
public interface JobDao {
    @Insert
    long insert(JobEntity job);

    @Update
    void update(JobEntity job);

    @Query("SELECT * FROM jobs WHERE authorId = :userId ORDER BY createdAt DESC")
    LiveData<List<JobEntity>> getJobsByAuthor(int userId);

    @Query("SELECT * FROM jobs WHERE authorName = :userName ORDER BY createdAt DESC")
    List<JobEntity> getJobsByAuthor(String userName);

    @Query("SELECT jobs.* FROM jobs JOIN applications ON jobs.id = applications.jobId WHERE applications.applicantName = :userName AND applications.status = 'accepted' ORDER BY jobs.createdAt DESC")
    LiveData<List<JobEntity>> getAcceptedJobs(String userName);

    @Query("SELECT jobs.* FROM jobs JOIN applications ON jobs.id = applications.jobId WHERE applications.applicantName = :userName AND applications.status = 'accepted' ORDER BY jobs.createdAt DESC")
    List<JobEntity> getAcceptedJobsSync(String userName);

    @Query("SELECT jobs.* FROM jobs JOIN applications ON jobs.id = applications.jobId WHERE applications.applicantName = :userName ORDER BY jobs.createdAt DESC")
    LiveData<List<JobEntity>> getAppliedJobs(String userName);

    @Query("SELECT jobs.* FROM jobs JOIN applications ON jobs.id = applications.jobId WHERE applications.applicantName = :userName ORDER BY jobs.createdAt DESC")
    List<JobEntity> getAppliedJobsSync(String userName);

    @Query("SELECT * FROM jobs WHERE city = :city ORDER BY createdAt DESC")
    LiveData<List<JobEntity>> getJobsByCity(String city);

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    JobEntity getById(int id);

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    List<JobEntity> getAllJobs();

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    LiveData<List<JobEntity>> getAllJobsLiveData();
}
