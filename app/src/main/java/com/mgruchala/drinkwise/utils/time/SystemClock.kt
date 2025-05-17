package com.mgruchala.drinkwise.utils.time

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
