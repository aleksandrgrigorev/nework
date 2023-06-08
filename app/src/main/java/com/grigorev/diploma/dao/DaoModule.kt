package com.grigorev.diploma.dao

import com.grigorev.diploma.db.PostsDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun providePostDao(db: PostsDb): PostDao = db.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: PostsDb): PostRemoteKeyDao = db.postRemoteKeyDao()
}