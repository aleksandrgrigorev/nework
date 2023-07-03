package com.grigorev.diploma.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.grigorev.diploma.dto.Job
import com.grigorev.diploma.dto.Post
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun getWallPosts(authorId: Int): Flow<PagingData<Post>>

    fun getAllJobs(): LiveData<List<Job>>

    suspend fun loadJobs(authorId: Int)

    suspend fun saveJob(job: Job)

    suspend fun deleteJobById(id: Int)
}