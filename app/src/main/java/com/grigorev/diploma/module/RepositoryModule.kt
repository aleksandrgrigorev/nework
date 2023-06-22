package com.grigorev.diploma.module

import com.grigorev.diploma.repository.EventRepository
import com.grigorev.diploma.repository.EventRepositoryImpl
import com.grigorev.diploma.repository.PostRepository
import com.grigorev.diploma.repository.PostRepositoryImpl
import com.grigorev.diploma.repository.UserRepository
import com.grigorev.diploma.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Singleton
    @Binds
    fun bindsEventRepository(impl: EventRepositoryImpl): EventRepository

    @Singleton
    @Binds
    fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository
}