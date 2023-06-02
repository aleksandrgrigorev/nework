package com.grigorev.diploma.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grigorev.diploma.dao.PostDao
import com.grigorev.diploma.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class PostsDb: RoomDatabase(){

    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: PostsDb? = null

        fun getInstance(context: Context): PostsDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context)
                    .also { instance = it }
            }
        }

        private fun buildDatabase(context: Context)
                = Room.databaseBuilder(context, PostsDb::class.java, "Posts.db")
            .build()
    }
}