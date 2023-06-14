package com.grigorev.diploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.dao.PostRemoteKeyDao
import com.grigorev.diploma.entity.PostEntity
import com.grigorev.diploma.entity.PostRemoteKeyEntity
import com.grigorev.diploma.util.Converters

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
    ], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}