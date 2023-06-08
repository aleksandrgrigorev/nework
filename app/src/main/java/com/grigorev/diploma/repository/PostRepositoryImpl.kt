package com.grigorev.diploma.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grigorev.diploma.api.Api
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dao.PostRemoteKeyDao
import com.grigorev.diploma.db.PostsDb
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.entity.PostEntity
import com.grigorev.diploma.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: Api,
    postRemoteKeyDao: PostRemoteKeyDao,
    postsDb: PostsDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val flow: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(apiService, postDao, postRemoteKeyDao, postsDb)
    ).flow
        .map { it.map(PostEntity::toDto) }

    override suspend fun getAll() {
        val body: List<Post>
        try {
            val response = apiService.getAllPosts()
            if (!response.isSuccessful) {
                throw Exception("Response was not successful")
            }
            body = response.body()!!
            postDao.insert(body.toEntity())
        } catch (e: Exception) {
            throw e
        }
    }
}
