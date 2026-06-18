package com.rustyn.sentinel.core.di

import com.rustyn.sentinel.data.repository.AllowlistRepositoryImpl
import com.rustyn.sentinel.data.repository.BlockedCallRepositoryImpl
import com.rustyn.sentinel.data.repository.RuleRepositoryImpl
import com.rustyn.sentinel.data.repository.SettingsRepositoryImpl
import com.rustyn.sentinel.data.repository.SuggestionRepositoryImpl
import com.rustyn.sentinel.domain.repository.AllowlistRepository
import com.rustyn.sentinel.domain.repository.BlockedCallRepository
import com.rustyn.sentinel.domain.repository.RuleRepository
import com.rustyn.sentinel.domain.repository.SettingsRepository
import com.rustyn.sentinel.domain.repository.SuggestionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(impl: RuleRepositoryImpl): RuleRepository

    @Binds
    @Singleton
    abstract fun bindBlockedCallRepository(impl: BlockedCallRepositoryImpl): BlockedCallRepository

    @Binds
    @Singleton
    abstract fun bindAllowlistRepository(impl: AllowlistRepositoryImpl): AllowlistRepository

    @Binds
    @Singleton
    abstract fun bindSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
