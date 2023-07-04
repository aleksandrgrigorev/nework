package com.grigorev.diploma.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grigorev.diploma.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {

    @Query("SELECT COUNT(*) == 0 FROM EventRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(`key`) FROM EventRemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT MIN(`key`) FROM EventRemoteKeyEntity")
    suspend fun min(): Int?

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: List<EventRemoteKeyEntity>)
}