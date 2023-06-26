package com.grigorev.diploma.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grigorev.diploma.entity.JobEntity

@Dao
interface JobDao {

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getAllJobs(): LiveData<List<JobEntity>>

    @Query("SELECT * FROM JobEntity WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: Int): JobEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(jobEntity: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobEntities: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :jobId")
    suspend fun removeJobById(jobId: Int)

    @Query("DELETE FROM JobEntity")
    suspend fun removeAllJobs()
}