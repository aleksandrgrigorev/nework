package com.grigorev.diploma.repository

import androidx.paging.PagingData
import com.grigorev.diploma.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun getAllPosts()

    suspend fun savePost(post: Post)
    suspend fun removePostById(id: Int)

    suspend fun likePostById(id: Int)

    suspend fun unlikePostById(id: Int)

}