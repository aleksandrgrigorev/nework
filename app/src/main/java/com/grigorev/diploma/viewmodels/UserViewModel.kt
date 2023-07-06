package com.grigorev.diploma.viewmodels

import androidx.lifecycle.*
import com.grigorev.diploma.api.UserApiService
import com.grigorev.diploma.dto.User
import com.grigorev.diploma.model.StateModel
import com.grigorev.diploma.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiService: UserApiService,
) : ViewModel() {

    val data: LiveData<List<User>> =
        userRepository.data
            .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _userIds = MutableLiveData<Set<Int>>()
    val userIds: LiveData<Set<Int>>
        get() = _userIds

    init {
        getUsers()
    }

    private fun getUsers() = viewModelScope.launch {
        _dataState.postValue(StateModel(loading = true))
        try {
            userRepository.getAll()
            _dataState.postValue(StateModel())
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun getUserById(id: Int) = viewModelScope.launch {
        _dataState.postValue(StateModel(loading = true))
        try {
            val response = userApiService.getUserById(id)
            if (response.isSuccessful) {
                _user.value = response.body()
            }
            _dataState.postValue(StateModel())
        } catch (e: Exception) {
            _dataState.postValue(StateModel(error = true))
        }
    }

    fun getUsersIds(set: Set<Int>) =
        viewModelScope.launch { _userIds.value = set }
}