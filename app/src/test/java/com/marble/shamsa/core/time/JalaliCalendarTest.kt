package com.marble.shamsa.core.time

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JalaliCalendarTest {
    @Test fun nowruz1404() {
        assertEquals(JalaliDate(1404, 1, 1), JalaliCalendar.fromGregorian(LocalDate.of(2025, 3, 21)))
        assertEquals(LocalDate.of(2025, 3, 21), JalaliCalendar.toGregorian(JalaliDate(1404, 1, 1)))
    }

    @Test fun contemporaryRegression() {
        assertEquals(JalaliDate(1405, 5, 25), JalaliCalendar.fromGregorian(LocalDate.of(2026, 8, 16)))
        assertEquals(LocalDate.of(2026, 8, 16), JalaliCalendar.toGregorian(JalaliDate(1405, 5, 25)))
    }

    @Test fun leapYears() {
        assertTrue(JalaliCalendar.isLeapYear(1399))
        assertFalse(JalaliCalendar.isLeapYear(1400))
    }

    @Test fun roundTrips() {
        listOf(
            JalaliDate(1399, 12, 30),
            JalaliDate(1400, 1, 1),
            JalaliDate(1403, 12, 30),
            JalaliDate(1404, 1, 1),
            JalaliDate(1405, 5, 25),
            JalaliDate(1500, 7, 12)
        ).forEach { date -> assertEquals(date, JalaliCalendar.fromGregorian(JalaliCalendar.toGregorian(date))) }
    }
}
