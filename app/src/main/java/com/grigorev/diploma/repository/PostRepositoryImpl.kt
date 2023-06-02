package com.grigorev.diploma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.grigorev.diploma.api.Api
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.entity.toEntity

class PostRepositoryImpl(private val dao: PostDao, private val apiService: Api) : PostRepository {

    override val flow = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(
                apiService
            )
        }
    ).flow

    override suspend fun getAll() {
        val body: List<Post>
        try {
            val response = Api.service.getAllPosts()
            if (!response.isSuccessful) {
                throw Exception("Response was not successful")
            }
            body = response.body()!!
            dao.insert(body.toEntity())
        } catch (e: Exception) {
            throw e
        }
    }
}
