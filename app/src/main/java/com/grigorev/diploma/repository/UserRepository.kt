package com.grigorev.diploma.repository

import com.grigorev.diploma.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val data: Flow<List<User>>

    suspend fun getAll()

}