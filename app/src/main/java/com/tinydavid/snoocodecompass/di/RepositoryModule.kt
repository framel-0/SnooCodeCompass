package com.tinydavid.snoocodecompass.di

import com.tinydavid.snoocodecompass.data.repositories.HealthCareRepositoryImpl
import com.tinydavid.snoocodecompass.domain.repositories.HealthCareRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindHealthCareRepository(impl: HealthCareRepositoryImpl): HealthCareRepository
}