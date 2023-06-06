package com.grigorev.diploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class PostsDb : RoomDatabase() {

    abstract fun postDao(): PostDao

}