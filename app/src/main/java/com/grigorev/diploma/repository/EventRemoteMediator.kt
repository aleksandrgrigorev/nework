package com.grigorev.diploma.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.grigorev.diploma.api.EventApiService
import com.grigorev.diploma.dao.EventDao
import com.grigorev.diploma.dao.EventRemoteKeyDao
import com.grigorev.diploma.db.AppDb
import com.grigorev.diploma.entity.EventEntity
import com.grigorev.diploma.entity.EventRemoteKeyEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val eventApiService: EventApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    eventApiService.getEventLatest(state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    eventApiService.getEventAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    eventApiService.getEventBefore(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val body = result.body() ?: throw Error(result.message())

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )

                        if (eventRemoteKeyDao.isEmpty()) {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                        eventDao.removeAll()
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id,
                                ),
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        if (body.isNotEmpty()) {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id,
                                ),
                            )
                        }

                    }
                }
                eventDao.insertEvents(body.map(EventEntity::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}