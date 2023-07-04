package com.grigorev.diploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WallRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Int,
) {
    enum class KeyType {
        AFTER,
        BEFORE
    }
}