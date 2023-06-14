package com.grigorev.diploma.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grigorev.diploma.api.PostsApiService
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dao.PostRemoteKeyDao
import com.grigorev.diploma.db.AppDb
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.entity.PostEntity
import com.grigorev.diploma.entity.toEntity
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.NetworkException
import com.grigorev.diploma.error.UnknownException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postsApiService: PostsApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(postsApiService, postDao, postRemoteKeyDao, appDb)
    ).flow
        .map { it.map(PostEntity::toDto) }

    override suspend fun getAllPosts() {
        val body: List<Post>
        try {
            val response = postsApiService.getAllPosts()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            body = response.body() ?: throw ApiException(response.code(), response.message())
            postDao.insert(body.toEntity())
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = postsApiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun removePostById(id: Int) {
        try {
            val response = postsApiService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            postDao.removePostById(id)
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}