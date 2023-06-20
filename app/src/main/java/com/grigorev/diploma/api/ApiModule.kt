package com.grigorev.diploma.api

import com.grigorev.diploma.BuildConfig
import com.grigorev.diploma.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = appAuth.state.value.token?.let { token ->
                chain.request()
                    .newBuilder()
                    .addHeader("Authorization", token)
                    .build()
            } ?: chain.request()

            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .build()

    @Provides
    @Singleton
    fun providePostsApiService(retrofit: Retrofit): PostsApiService =
        retrofit.create(PostsApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideEventApiService(retrofit: Retrofit): EventApiService =
        retrofit.create(EventApiService::class.java)
}