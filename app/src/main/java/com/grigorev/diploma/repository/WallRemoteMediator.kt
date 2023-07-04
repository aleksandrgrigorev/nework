package com.grigorev.diploma.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.grigorev.diploma.dao.WallPostDao
import com.grigorev.diploma.entity.WallRemoteKeyEntity
import com.grigorev.diploma.api.PostsApiService
import com.grigorev.diploma.dao.WallRemoteKeyDao
import com.grigorev.diploma.db.AppDb
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.entity.WallPostEntity
import com.grigorev.diploma.entity.toWallPostEntity
import com.grigorev.diploma.error.ApiException

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator(
    private val postsApiService: PostsApiService,
    private val wallPostDao: WallPostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val appDb: AppDb,
    private val authorId: Int,
) : RemoteMediator<Int, WallPostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, WallPostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    postsApiService.getLatestWallPosts(authorId, state.config.initialLoadSize)
                }

                LoadType.PREPEND -> {
                    val maxKey = wallRemoteKeyDao.max() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    postsApiService.getWallPostsAfter(maxKey, authorId, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val minKey = wallRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    postsApiService.getWallPostsBefore(minKey, authorId, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) throw ApiException(response.code(), response.message())
            val body = response.body() ?: throw Error(response.message())

            if (body.isEmpty()) return MediatorResult.Success(
                endOfPaginationReached = true
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        wallRemoteKeyDao.clear()
                        insertMaxKey(body)
                        insertMinKey(body)
                        wallPostDao.removeAllPosts()
                    }
                    LoadType.PREPEND -> insertMaxKey(body)
                    LoadType.APPEND -> insertMinKey(body)
                }
                wallPostDao.insertPosts(body.toWallPostEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun insertMaxKey(body: List<Post>) {
        wallRemoteKeyDao.insertKey(
            WallRemoteKeyEntity(
                WallRemoteKeyEntity.KeyType.AFTER,
                body.first().id
            )
        )
    }

    private suspend fun insertMinKey(body: List<Post>) {
        wallRemoteKeyDao.insertKey(
            WallRemoteKeyEntity(
                WallRemoteKeyEntity.KeyType.BEFORE,
                body.last().id,
            )
        )
    }
}
