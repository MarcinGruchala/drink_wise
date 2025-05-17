package com.mgruchala.drinkwise.utils.time

/** Provides the current time in milliseconds since the epoch. */
interface Clock {
    fun nowMillis(): Long
}

