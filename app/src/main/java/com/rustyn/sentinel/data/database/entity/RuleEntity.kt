package com.rustyn.sentinel.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String,
    val type: String, // EXACT, PREFIX, WILDCARD
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val description: String? = null,
    val startTime: String? = null, // "HH:mm"
    val endTime: String? = null, // "HH:mm"
    val daysOfWeek: String? = null // e.g., "Mon,Tue,Wed"
)
