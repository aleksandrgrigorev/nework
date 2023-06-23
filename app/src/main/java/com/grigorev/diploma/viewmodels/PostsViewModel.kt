package com.grigorev.diploma.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.grigorev.diploma.auth.AppAuth
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.dto.MediaModel
import com.grigorev.diploma.dto.MediaUpload
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.model.StateModel
import com.grigorev.diploma.repository.PostRepository
import com.grigorev.diploma.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

val emptyPost = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    authorId = 0,
    authorJob = null,
    content = "",
    likedByMe = false,
    link = null,
    mentionIds = emptySet(),
    mentionedMe = false,
    likeOwnerIds = emptySet(),
    ownedByMe = false,
    published = "",
    attachment = null
)

private val noMedia = MediaModel(null, null, null)

@ExperimentalCoroutinesApi
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> =
        appAuth.state
            .flatMapLatest { (myId, _) ->
                cached.map { pagingData ->
                    pagingData.map { post ->
                        post.copy(
                            ownedByMe = post.authorId == myId,
                            likedByMe = post.likeOwnerIds.contains(myId)
                        )
                    }
                }
            }

    private val _dataState = MutableLiveData(StateModel())
    val dataState: LiveData<StateModel>
        get() = _dataState

    val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _error = SingleLiveEvent<Throwable>()
    val error: LiveData<Throwable>
        get() = _error

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    fun removeById(id: Int) = viewModelScope.launch {
        try {
            _dataState.value = StateModel(refreshing = true)
            repository.removePostById(id)
            _dataState.value = StateModel()
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun likeById(id: Int) = viewModelScope.launch {
        try {
            repository.likePostById(id)
            _dataState.value = StateModel()
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun unlikeById(id: Int) = viewModelScope.launch {
        try {
            repository.unlikePostById(id)
            _dataState.value = StateModel()
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_media.value) {
                        noMedia -> repository.savePost(post)
                        else -> _media.value?.inputStream?.let {
                            MediaUpload(it) }?.let {
                            repository.saveWithAttachment(post, it, _media.value?.type!!)
                        }
                    }
                    _dataState.value = StateModel()
                } catch (e: Exception) {
                    _dataState.value = StateModel(error = true)
                }
            }
        }
        edited.value = emptyPost
        _media.value = noMedia
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String, link: String) {
        val text = content.trim()

        if (edited.value?.content != text) {
            edited.value = edited.value?.copy(content = text)
        }
        if (edited.value?.link != link) {
            edited.value = edited.value?.copy(link = link)
        }
    }

    fun changeMedia(uri: Uri?, inputStream: InputStream?, type: AttachmentType?) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun removeMedia() {
        _media.value = noMedia
    }

}