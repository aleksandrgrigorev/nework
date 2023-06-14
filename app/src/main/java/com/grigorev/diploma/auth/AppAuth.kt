package com.grigorev.diploma.auth

import android.content.Context
import androidx.core.content.edit
import com.grigorev.diploma.api.UserApiService
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.NetworkException
import com.grigorev.diploma.error.UnknownException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val userApiService: UserApiService
) {

    private val idKey = "ID_KEY"
    private val tokenKey = "TOKEN_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState?>

    init {
        val id = prefs.getInt(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        _state = if (token == null || !prefs.contains(idKey)) {
            prefs.edit { clear() }
            MutableStateFlow(null)
        } else {
            MutableStateFlow(AuthState(id, token))
        }
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Int, token: String) {
        prefs.edit {
            putInt(idKey, id)
            putString(tokenKey, token)
        }
        _state.value = AuthState(id, token)
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit { clear() }
        _state.value = null
    }

    suspend fun update(login: String, password: String) {
        try {
            val response = userApiService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            println(e)
            throw UnknownException
        }
    }


    suspend fun registerUser(login: String, password: String, name: String, file: File) {
        try {
            val fileData = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val response = userApiService.registerUser(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                fileData
            )

            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}

data class AuthState(
    val id: Int = 0,
    val token: String = "",
)