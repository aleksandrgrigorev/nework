package com.grigorev.diploma.auth

import android.content.Context
import androidx.core.content.edit
import com.grigorev.diploma.api.UserApiService
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.error.NetworkException
import com.grigorev.diploma.error.UnknownException
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
) {

    private val idKey = "ID_KEY"
    private val tokenKey = "TOKEN_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState>

    init {
        val id = prefs.getInt(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0 || token == null) {
            _state = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _state = MutableStateFlow(AuthState(id, token))
        }
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Int, token: String?) {
        _state.value = AuthState(id, token)
        prefs.edit {
            putInt(idKey, id)
            putString(tokenKey, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth() {
        _state.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): UserApiService
    }

    suspend fun update(login: String, password: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().updateUser(login, password)
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
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().registerUser(
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
    val token: String? = null,
)