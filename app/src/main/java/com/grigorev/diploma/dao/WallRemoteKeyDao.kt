package com.grigorev.diploma.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grigorev.diploma.entity.WallRemoteKeyEntity

@Dao
interface WallRemoteKeyDao {

    @Query("SELECT MIN(id) FROM WallRemoteKeyEntity")
    suspend fun getMinKey(): Int?

    @Query("SELECT MAX(id) FROM WallRemoteKeyEntity")
    suspend fun getMaxKey(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(wallRemoteKeyEntity: WallRemoteKeyEntity)

    @Query("DELETE FROM WallRemoteKeyEntity")
    suspend fun clear()
}