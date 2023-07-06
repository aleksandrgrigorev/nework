package com.grigorev.diploma.repository

import com.grigorev.diploma.api.UserApiService
import com.grigorev.diploma.dao.UserDao
import com.grigorev.diploma.dto.User
import com.grigorev.diploma.entity.toDto
import com.grigorev.diploma.entity.toUserEntity
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.NetworkException
import com.grigorev.diploma.error.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApiService: UserApiService,
) : UserRepository {

    override val data: Flow<List<User>> =
        userDao.getAll()
            .map { it.toDto() }
            .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            userDao.getAll()
            val response = userApiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            userDao.insert(body.toUserEntity())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}