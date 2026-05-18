package com.mgruchala.drinkwise.presentation.daydetails.editor

import com.mgruchala.alcohol_database.DrinkEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DrinkEditorStateTest {

    private val zoneId: ZoneId = ZoneId.of("Europe/Warsaw")
    private val selectedDate: LocalDate = LocalDate.of(2026, 5, 17)

    @Test
    fun `add draft uses current clock time without seconds`() {
        val currentMillis = LocalDateTime.of(2026, 5, 18, 14, 37, 42)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        val draft = DrinkEditorDraft.forAdd(currentTimeMillis = currentMillis, zoneId = zoneId)

        assertEquals(LocalTime.of(14, 37), draft.time)
        assertEquals("1", draft.numberOfDrinksText)
    }

    @Test
    fun `edit draft loads drink values and timestamp time`() {
        val drink = DrinkEntity(
            uid = 42,
            quantity = 175,
            alcoholContent = 13.5f,
            timestamp = LocalDateTime.of(2026, 5, 17, 20, 15)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )

        val draft = DrinkEditorDraft.forEdit(drink = drink, zoneId = zoneId)

        assertEquals("175", draft.quantityMlText)
        assertEquals("13.5", draft.abvText)
        assertEquals("1", draft.numberOfDrinksText)
        assertEquals(LocalTime.of(20, 15), draft.time)
    }

    @Test
    fun `draft is valid for positive quantity abv in range and positive count`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "5.2",
            numberOfDrinksText = "2",
            time = LocalTime.of(18, 30)
        )

        assertTrue(draft.isValidForAdd)
        assertTrue(draft.isValidForEdit)
    }

    @Test
    fun `draft rejects non-positive quantity`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "0",
            abvText = "5",
            numberOfDrinksText = "1",
            time = LocalTime.of(18, 30)
        )

        assertFalse(draft.isValidForAdd)
        assertFalse(draft.isValidForEdit)
    }

    @Test
    fun `draft rejects abv outside inclusive zero to one hundred range`() {
        val tooHigh = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "100.1",
            numberOfDrinksText = "1",
            time = LocalTime.of(18, 30)
        )
        val zeroAbv = tooHigh.copy(abvText = "0")

        assertFalse(tooHigh.isValidForAdd)
        assertTrue(zeroAbv.isValidForAdd)
    }

    @Test
    fun `draft rejects non-positive add count`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "5",
            numberOfDrinksText = "0",
            time = LocalTime.of(18, 30)
        )

        assertFalse(draft.isValidForAdd)
        assertTrue(draft.isValidForEdit)
    }

    @Test
    fun `compose timestamp keeps time inside selected day`() {
        val millis = composeDrinkTimestamp(
            selectedDate = selectedDate,
            time = LocalTime.of(23, 59),
            zoneId = zoneId
        )

        val restored = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()

        assertEquals(LocalDateTime.of(2026, 5, 17, 23, 59), restored)
    }

    @Test
    fun `to add drinks creates separate drink rows with selected timestamp`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "330",
            abvText = "4.8",
            numberOfDrinksText = "3",
            time = LocalTime.of(21, 5)
        )

        val drinks = draft.toAddDrinks(selectedDate = selectedDate, zoneId = zoneId)

        assertEquals(3, drinks.size)
        assertTrue(drinks.all { it.uid == 0 })
        assertTrue(drinks.all { it.quantity == 330 })
        assertTrue(drinks.all { it.alcoholContent == 4.8f })
        assertTrue(drinks.all {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDateTime() ==
                LocalDateTime.of(2026, 5, 17, 21, 5)
        })
    }

    @Test
    fun `to updated drink preserves uid and selected date`() {
        val original = DrinkEntity(
            uid = 7,
            quantity = 500,
            alcoholContent = 5f,
            timestamp = LocalDateTime.of(2026, 5, 17, 18, 30)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )
        val draft = DrinkEditorDraft(
            quantityMlText = "250",
            abvText = "12.5",
            numberOfDrinksText = "9",
            time = LocalTime.of(22, 10)
        )

        val updated = draft.toUpdatedDrink(
            original = original,
            selectedDate = selectedDate,
            zoneId = zoneId
        )

        assertEquals(7, updated.uid)
        assertEquals(250, updated.quantity)
        assertEquals(12.5f, updated.alcoholContent)
        assertEquals(
            LocalDateTime.of(2026, 5, 17, 22, 10),
            Instant.ofEpochMilli(updated.timestamp).atZone(zoneId).toLocalDateTime()
        )
    }
}
