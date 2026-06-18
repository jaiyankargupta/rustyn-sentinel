package com.rustyn.sentinel.core.di

import android.content.Context
import androidx.room.Room
import com.rustyn.sentinel.data.database.SentinelDatabase
import com.rustyn.sentinel.data.database.dao.AllowlistDao
import com.rustyn.sentinel.data.database.dao.BlockedCallDao
import com.rustyn.sentinel.data.database.dao.RuleDao
import com.rustyn.sentinel.data.database.dao.SettingsDao
import com.rustyn.sentinel.data.database.dao.SuggestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SentinelDatabase {
        return Room.databaseBuilder(
            context,
            SentinelDatabase::class.java,
            SentinelDatabase.DATABASE_NAME
        ).addMigrations(SentinelDatabase.MIGRATION_1_2)
         .fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideRuleDao(database: SentinelDatabase): RuleDao = database.ruleDao()

    @Provides
    fun provideBlockedCallDao(database: SentinelDatabase): BlockedCallDao = database.blockedCallDao()

    @Provides
    fun provideAllowlistDao(database: SentinelDatabase): AllowlistDao = database.allowlistDao()

    @Provides
    fun provideSuggestionDao(database: SentinelDatabase): SuggestionDao = database.suggestionDao()

    @Provides
    fun provideSettingsDao(database: SentinelDatabase): SettingsDao = database.settingsDao()
}
