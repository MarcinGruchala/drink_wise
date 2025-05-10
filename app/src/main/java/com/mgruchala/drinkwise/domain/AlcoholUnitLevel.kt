package com.mgruchala.drinkwise.domain

sealed class AlcoholUnitLevel(open val unitCount: Float, open val limit: Float) {
    data class Low(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    data class Alarming(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    data class High(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    companion object {
        fun fromUnitCount(unitCount: Float, limit: Float): AlcoholUnitLevel {
            return when {
                unitCount <= 0.7 * limit -> Low(unitCount, limit)
                unitCount < limit -> Alarming(unitCount, limit)
                else -> High(unitCount, limit)
            }
        }
    }
}
