package com.mgruchala.drinkwise.utils.time

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DateTimeModule {

    @Binds
    @Singleton
    abstract fun bindClock(systemClock: SystemClock): Clock
}
