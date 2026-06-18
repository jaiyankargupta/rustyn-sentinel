package com.rustyn.sentinel.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allowlist")
data class AllowlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String,
    val type: String, // EXACT, PREFIX
    val createdAt: Long = System.currentTimeMillis(),
    val name: String? = null
)
