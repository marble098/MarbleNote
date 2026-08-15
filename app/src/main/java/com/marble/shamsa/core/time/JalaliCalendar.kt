package com.marble.shamsa.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Civil Solar Hijri (Jalali) conversion for the supported application range.
 *
 * This implementation uses the well-known Jalali leap-year break-point algorithm rather than
 * a simple 2820-year arithmetic approximation. That distinction matters around Nowruz.
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(year in JalaliCalendar.MIN_YEAR..JalaliCalendar.MAX_YEAR)
        require(month in 1..12)
        require(day in 1..JalaliCalendar.daysInMonth(year, month))
    }
}

object JalaliCalendar {
    const val MIN_YEAR = -60
    const val MAX_YEAR = 3177

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    fun fromEpochMillis(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): JalaliDate =
        fromGregorian(Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate())

    fun toEpochMillis(
        date: JalaliDate,
        hour: Int,
        minute: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        require(hour in 0..23 && minute in 0..59)
        return toGregorian(date).atTime(hour, minute).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun fromGregorian(date: LocalDate): JalaliDate = d2j(g2d(date.year, date.monthValue, date.dayOfMonth))

    fun toGregorian(date: JalaliDate): LocalDate {
        val (year, month, day) = d2g(j2d(date.year, date.month, date.day))
        return LocalDate.of(year, month, day)
    }

    fun isLeapYear(year: Int): Boolean = jalCal(year).leap == 0

    fun daysInMonth(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapYear(year)) 30 else 29
        else -> error("Invalid Jalali month")
    }

    fun monthName(month: Int, persian: Boolean): String {
        require(month in 1..12)
        val fa = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        val en = listOf("Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand")
        return (if (persian) fa else en)[month - 1]
    }

    private fun jalCal(jy: Int): JalCal {
        require(jy in MIN_YEAR..MAX_YEAR) { "Jalali year out of supported range: $jy" }
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        var jm = 0

        var i = 1
        while (i < breaks.size) {
            jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
            i++
        }

        var n = jy - jp
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ++

        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCal(leap, gy, march)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Long {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1L) * 31L - div(jm, 7).toLong() * (jm - 7L) + jd - 1L
    }

    private fun d2j(jdn: Long): JalaliDate {
        val (gy, _, _) = d2g(jdn)
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r.march)
        var k = (jdn - jdn1f).toInt()

        if (k >= 0) {
            if (k <= 185) {
                val jm = 1 + div(k, 31)
                val jd = mod(k, 31) + 1
                return JalaliDate(jy, jm, jd)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        val jm = 7 + div(k, 30)
        val jd = mod(k, 30) + 1
        return JalaliDate(jy, jm, jd)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Long {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4).toLong()
        d += div(153 * mod(gm + 9, 12) + 2, 5)
        d += gd - 34840408L
        d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4)
        return d + 752
    }

    private fun d2g(jdn: Long): Triple<Int, Int, Int> {
        var j = 4L * jdn + 139361631L
        j += divLong(divLong(4L * jdn + 183187720L, 146097L) * 3L, 4L) * 4L - 3908L
        val i = divLong(modLong(j, 1461L), 4L) * 5L + 308L
        val gd = (divLong(modLong(i, 153L), 5L) + 1L).toInt()
        val gm = (modLong(divLong(i, 153L), 12L) + 1L).toInt()
        val gy = (divLong(j, 1461L) - 100100L + div(8 - gm, 6)).toInt()
        return Triple(gy, gm, gd)
    }

    private fun div(a: Int, b: Int): Int = a / b
    private fun mod(a: Int, b: Int): Int = a - (a / b) * b
    private fun divLong(a: Long, b: Long): Long = a / b
    private fun modLong(a: Long, b: Long): Long = a - (a / b) * b
}
