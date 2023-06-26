package com.grigorev.diploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grigorev.diploma.dao.WallPostDao
import com.grigorev.diploma.dao.EventDao
import com.grigorev.diploma.dao.EventRemoteKeyDao
import com.grigorev.diploma.dao.JobDao
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dao.PostRemoteKeyDao
import com.grigorev.diploma.dao.WallRemoteKeyDao
import com.grigorev.diploma.entity.EventEntity
import com.grigorev.diploma.entity.EventRemoteKeyEntity
import com.grigorev.diploma.entity.JobEntity
import com.grigorev.diploma.entity.PostEntity
import com.grigorev.diploma.entity.PostRemoteKeyEntity
import com.grigorev.diploma.entity.WallPostEntity
import com.grigorev.diploma.entity.WallRemoteKeyEntity
import com.grigorev.diploma.util.Converters

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        JobEntity::class,
        WallPostEntity::class,
        WallRemoteKeyEntity::class,
    ], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao

    abstract fun jobDao(): JobDao

    abstract fun wallPostDao(): WallPostDao
    abstract fun wallRemoteKeyDao(): WallRemoteKeyDao
}