package com.grigorev.diploma.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grigorev.diploma.api.JobsApiService
import com.grigorev.diploma.api.PostsApiService
import com.grigorev.diploma.dao.JobDao
import com.grigorev.diploma.dao.WallPostDao
import com.grigorev.diploma.dao.WallRemoteKeyDao
import com.grigorev.diploma.db.AppDb
import com.grigorev.diploma.dto.Job
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.entity.JobEntity
import com.grigorev.diploma.entity.fromDto
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.DbException
import com.grigorev.diploma.error.NetworkException
import com.grigorev.diploma.error.UnknownException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val postsApiService: PostsApiService,
    private val jobsApiService: JobsApiService,
    private val appDb: AppDb,
    private val wallPostDao: WallPostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val jobDao: JobDao,
) : ProfileRepository {

    @ExperimentalPagingApi
    override fun getWallPosts(authorId: Int): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = DEFAULT_WALL_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(
            postsApiService,
            wallPostDao,
            wallRemoteKeyDao,
            appDb,
            authorId
        ),
        pagingSourceFactory = { wallPostDao.getWallPagingSource() }
    ).flow.map { postList ->
        postList.map { it.toDto() }
    }

    override fun getAllJobs(): LiveData<List<Job>> = jobDao.getAllJobs().map { jobList ->
        jobList.map { it.toDto() }
    }

    override suspend fun loadJobs(authorId: Int) {
        try {
            jobDao.removeAllJobs()
            val response = jobsApiService.getJobsById(authorId)
            if (!response.isSuccessful) throw ApiException(response.code(), response.message())
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            jobDao.insertJobs(body.fromDto())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: SQLException) {
            throw DbException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = jobsApiService.saveMyJob(job)
            if (!response.isSuccessful) throw ApiException(response.code(), response.message())
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            jobDao.insertJob(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: SQLException) {
            throw DbException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun deleteJobById(id: Int) {
        val jobToDelete = jobDao.getJobById(id)
        try {
            jobDao.removeJobById(id)

            val response = jobsApiService.removeMyJobById(id)
            if (!response.isSuccessful) {
                jobDao.insertJob(jobToDelete)
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: SQLException) {
            throw DbException
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}
