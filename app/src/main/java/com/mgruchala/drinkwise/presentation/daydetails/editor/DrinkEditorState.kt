package com.mgruchala.drinkwise.presentation.daydetails.editor

import com.mgruchala.alcohol_database.DrinkEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class DrinkEditorDraft(
    val quantityMlText: String = "",
    val abvText: String = "",
    val numberOfDrinksText: String = "1",
    val time: LocalTime
) {
    val quantityMl: Int?
        get() = quantityMlText.toIntOrNull()?.takeIf { it > 0 }

    val abv: Float?
        get() = abvText.toFloatOrNull()?.takeIf { it in 0f..100f }

    val numberOfDrinks: Int?
        get() = numberOfDrinksText.toIntOrNull()?.takeIf { it > 0 }

    val isValidForAdd: Boolean
        get() = quantityMl != null && abv != null && numberOfDrinks != null

    val isValidForEdit: Boolean
        get() = quantityMl != null && abv != null

    fun incrementCount(): DrinkEditorDraft {
        val next = (numberOfDrinks ?: 1) + 1
        return copy(numberOfDrinksText = next.toString())
    }

    fun decrementCount(): DrinkEditorDraft {
        val current = numberOfDrinks ?: 1
        val next = (current - 1).coerceAtLeast(1)
        return copy(numberOfDrinksText = next.toString())
    }

    fun toAddDrinks(
        selectedDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<DrinkEntity> {
        require(isValidForAdd) { "Cannot create drinks from invalid add draft." }
        val timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time, zoneId = zoneId)
        return List(requireNotNull(numberOfDrinks)) {
            DrinkEntity(
                uid = 0,
                quantity = requireNotNull(quantityMl),
                alcoholContent = requireNotNull(abv),
                timestamp = timestamp
            )
        }
    }

    fun toUpdatedDrink(
        original: DrinkEntity,
        selectedDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DrinkEntity {
        require(isValidForEdit) { "Cannot update drink from invalid edit draft." }
        return original.copy(
            quantity = requireNotNull(quantityMl),
            alcoholContent = requireNotNull(abv),
            timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time, zoneId = zoneId)
        )
    }

    companion object {
        fun forAdd(
            currentTimeMillis: Long,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): DrinkEditorDraft {
            val currentTime = Instant.ofEpochMilli(currentTimeMillis)
                .atZone(zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
            return DrinkEditorDraft(time = currentTime)
        }

        fun forEdit(
            drink: DrinkEntity,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): DrinkEditorDraft {
            val localTime = Instant.ofEpochMilli(drink.timestamp)
                .atZone(zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
            return DrinkEditorDraft(
                quantityMlText = drink.quantity.toString(),
                abvText = drink.alcoholContent.toString(),
                numberOfDrinksText = "1",
                time = localTime
            )
        }
    }
}

fun composeDrinkTimestamp(
    selectedDate: LocalDate,
    time: LocalTime,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return LocalDateTime.of(selectedDate, time.withSecond(0).withNano(0))
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}
