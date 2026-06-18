package com.rustyn.sentinel.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rustyn.sentinel.data.database.dao.AllowlistDao
import com.rustyn.sentinel.data.database.dao.BlockedCallDao
import com.rustyn.sentinel.data.database.dao.RuleDao
import com.rustyn.sentinel.data.database.dao.SettingsDao
import com.rustyn.sentinel.data.database.dao.SuggestionDao
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.data.database.entity.SettingsEntity
import com.rustyn.sentinel.data.database.entity.SuggestionEntity

@Database(
    entities = [
        RuleEntity::class,
        BlockedCallEntity::class,
        AllowlistEntity::class,
        SuggestionEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun allowlistDao(): AllowlistDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "sentinel_database"

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rules ADD COLUMN startTime TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE rules ADD COLUMN endTime TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE rules ADD COLUMN daysOfWeek TEXT DEFAULT NULL")
            }
        }
    }
}
