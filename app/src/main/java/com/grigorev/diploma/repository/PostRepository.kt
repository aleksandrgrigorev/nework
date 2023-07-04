package com.grigorev.diploma.repository

import androidx.paging.PagingData
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.dto.Media
import com.grigorev.diploma.dto.MediaUpload
import com.grigorev.diploma.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun savePost(post: Post)

    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, type: AttachmentType)

    suspend fun upload(upload: MediaUpload): Media

    suspend fun removePostById(id: Int)

    suspend fun likePostById(id: Int)

    suspend fun unlikePostById(id: Int)

}