package com.grigorev.diploma.repository

import androidx.paging.PagingData
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.dto.Media
import com.grigorev.diploma.dto.MediaUpload
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val data: Flow<PagingData<Event>>

    suspend fun saveEvent(event: Event)

    suspend fun saveWithAttachment(event: Event, upload: MediaUpload)

    suspend fun uploadWithContent(upload: MediaUpload): Media

    suspend fun removeById(id: Int)

    suspend fun likeById(id: Int)

    suspend fun unlikeById(id: Int)

    suspend fun participate(id: Int)

    suspend fun doNotParticipate(id: Int)
}