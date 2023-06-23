package com.grigorev.diploma.repository

import androidx.paging.*
import com.grigorev.diploma.api.EventApiService
import com.grigorev.diploma.dao.EventDao
import com.grigorev.diploma.dao.EventRemoteKeyDao
import com.grigorev.diploma.db.AppDb
import com.grigorev.diploma.dto.Attachment
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.dto.Media
import com.grigorev.diploma.dto.MediaUpload
import com.grigorev.diploma.entity.EventEntity
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.NetworkException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventApiService: EventApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(eventApiService, eventDao, eventRemoteKeyDao, appDb,
        )
    ).flow
        .map {
            it.map(EventEntity::toDto)
        }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = eventApiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            eventDao.insertEvent(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun saveWithAttachment(event: Event, upload: MediaUpload) {
        try {
            val media = uploadWithContent(upload)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            saveEvent(eventWithAttachment)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun uploadWithContent(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file",
                "name",
                upload.inputStream.readBytes()
                    .toRequestBody("*/*".toMediaTypeOrNull())
            )

            val response = eventApiService.uploadMedia(media)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun removeById(id: Int) {
        try {
            eventDao.removeById(id)
            val response = eventApiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun likeById(id: Int) {
        try {
            eventDao.likeById(id)
            val response = eventApiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())

            eventDao.insertEvent(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun unlikeById(id: Int) {
        try {
            eventDao.unlikeById(id)
            val response = eventApiService.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())

            eventDao.insertEvent(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun participate(id: Int) {
        try {
            eventDao.participate(id)
            val response = eventApiService.participate(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())

            eventDao.insertEvent(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun doNotParticipate(id: Int) {
        try {
            eventDao.doNotParticipate(id)
            val response = eventApiService.doNotParticipate(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())

            eventDao.insertEvent(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownError()
        }
    }
}