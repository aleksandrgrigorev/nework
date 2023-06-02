package com.grigorev.diploma.repository

import androidx.paging.PagingData
import com.grigorev.diploma.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val flow: Flow<PagingData<Post>>

    suspend fun getAll()
}