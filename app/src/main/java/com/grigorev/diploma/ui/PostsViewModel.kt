package com.grigorev.diploma.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.grigorev.diploma.api.Api
import com.grigorev.diploma.db.PostsDb
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.repository.PostRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PostsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryImpl(PostsDb.getInstance(application).postDao(), Api.service)
    val flow : Flow<PagingData<Post>> = repository.flow
        .flowOn(Dispatchers.Default)

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            repository.getAll()
        } catch (e: Exception) {
            throw e
        }
    }

}