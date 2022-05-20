package com.tinydavid.snoocodecompass.di

import android.app.Application
import com.tinydavid.snoocodecompass.data.AccelerometerSensor
import com.tinydavid.snoocodecompass.data.MagnetometerSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    fun provideAccelerometerSensor(appContext: Application): AccelerometerSensor {
        return AccelerometerSensor(appContext)
    }

    @Provides
    @Singleton
    fun provideMagnetometerSensor(appContext: Application): MagnetometerSensor {
        return MagnetometerSensor(appContext)
    }

}