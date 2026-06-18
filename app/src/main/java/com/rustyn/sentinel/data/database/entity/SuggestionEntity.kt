package com.rustyn.sentinel.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val suggestedPattern: String,
    val type: String = "PREFIX", // PREFIX
    val triggerCount: Int,
    val exampleNumbers: String, // Comma separated list of phone numbers
    val status: String = "PENDING", // PENDING, ACCEPTED, IGNORED
    val createdAt: Long = System.currentTimeMillis()
)
