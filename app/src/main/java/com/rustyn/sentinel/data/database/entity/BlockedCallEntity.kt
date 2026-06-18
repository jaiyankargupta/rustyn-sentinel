package com.rustyn.sentinel.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_calls")
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: String,
    val timestamp: Long = System.currentTimeMillis(),
    val matchedRuleId: Int? = null,
    val matchedRulePattern: String? = null,
    val blockAction: String = "REJECT" // REJECT, VOICEMAIL
)
