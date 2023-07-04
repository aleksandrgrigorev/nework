package com.grigorev.diploma.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grigorev.diploma.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

}