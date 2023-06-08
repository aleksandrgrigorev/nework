package com.grigorev.diploma.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.grigorev.diploma.api.Api
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dao.PostRemoteKeyDao
import com.grigorev.diploma.db.PostsDb
import com.grigorev.diploma.entity.PostEntity
import com.grigorev.diploma.entity.PostRemoteKeyEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: Api,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val postsDb: PostsDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> { apiService.getLatestPosts(state.config.initialLoadSize) }

                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getPostsAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getPostsBefore(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) { throw HttpException(result) }
            val body = result.body() ?: throw Error(result.message())

            postsDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.clear()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        )
                        postDao.clear()
                    }

                    LoadType.PREPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                ),
                            )
                        }
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                ),
                            )
                        }
                    }
                }

                postDao.insert(body.map(PostEntity.Companion::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}