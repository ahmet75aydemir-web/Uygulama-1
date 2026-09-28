package com.example

import com.example.data.model.HolidayCategory
import com.example.data.model.HolidayItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCountdownCalculation() {
        val now = 1000000000000L
        val target = now + (2 * 24 * 3600 * 1000L) + (5 * 3600 * 1000L) + (30 * 60 * 1000L) + (45 * 1000L)

        val item = HolidayItem(
            id = "test_holiday",
            title = "Test Tatil",
            subtitle = "Test",
            category = HolidayCategory.NATIONAL,
            targetEpochMillis = target,
            durationDays = 1.0,
            description = "Test açıklama"
        )

        val countdown = item.calculateCountdown(now)
        assertEquals(2L, countdown.days)
        assertEquals(5L, countdown.hours)
        assertEquals(30L, countdown.minutes)
        assertEquals(45L, countdown.seconds)
        assertFalse(countdown.isPassed)
        assertFalse(countdown.isToday)
    }
}
