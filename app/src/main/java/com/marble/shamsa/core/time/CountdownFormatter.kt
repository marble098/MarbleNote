package com.marble.shamsa.core.time

import kotlin.math.abs

object CountdownFormatter {
    data class Parts(
        val overdue: Boolean,
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long
    )

    fun parts(dueAtMillis: Long, now: Long): Parts {
        val overdue = dueAtMillis < now
        var remaining = abs(dueAtMillis - now) / 1000L
        val days = remaining / 86400L
        remaining %= 86400L
        val hours = remaining / 3600L
        remaining %= 3600L
        val minutes = remaining / 60L
        val seconds = remaining % 60L
        return Parts(overdue, days, hours, minutes, seconds)
    }

    private fun digits(value: Long, persian: Boolean): String =
        digits(value.toString(), persian)

    private fun digits(value: String, persian: Boolean): String {
        if (!persian) return value
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val out = StringBuilder(value.length)
        value.forEach { ch ->
            if (ch in '0'..'9') out.append(fa[ch - '0']) else out.append(ch)
        }
        return out.toString()
    }

    private fun pad2(value: Long, persian: Boolean): String =
        digits(value.toString().padStart(2, '0'), persian)

    private fun unit(
        value: Long,
        persian: Boolean,
        fa: String,
        en: String
    ): String =
        if (persian) {
            "${digits(value, true)} $fa"
        } else {
            "${digits(value, false)} $en"
        }

    private fun overdueLabel(persian: Boolean): String =
        if (persian) "گذشته" else "Overdue"

    private fun leftLabel(persian: Boolean): String =
        if (persian) "مانده" else "left"

    fun compact(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        return when {
            p.days > 0 ->
                if (persian) {
                    "${unit(p.days, true, "روز", "day")} و " +
                        unit(p.hours, true, "ساعت", "h")
                } else {
                    "${digits(p.days, false)}d ${digits(p.hours, false)}h"
                }

            p.hours > 0 ->
                if (persian) {
                    "${unit(p.hours, true, "ساعت", "h")} و " +
                        unit(p.minutes, true, "دقیقه", "m")
                } else {
                    "${digits(p.hours, false)}h ${digits(p.minutes, false)}m"
                }

            p.minutes > 0 ->
                if (persian) {
                    "${unit(p.minutes, true, "دقیقه", "m")} و " +
                        unit(p.seconds, true, "ثانیه", "s")
                } else {
                    "${digits(p.minutes, false)}m ${digits(p.seconds, false)}s"
                }

            else ->
                unit(p.seconds, persian, "ثانیه", "s")
        }
    }

    fun digital(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        return if (p.days > 0) {
            if (persian) {
                "${digits(p.days, true)} روز • " +
                    "${pad2(p.hours, true)}:${pad2(p.minutes, true)}"
            } else {
                "${digits(p.days, false)}d • " +
                    "${pad2(p.hours, false)}:${pad2(p.minutes, false)}"
            }
        } else {
            "${pad2(p.hours, persian)}:" +
                "${pad2(p.minutes, persian)}:" +
                pad2(p.seconds, persian)
        }
    }

    fun digitalLegend(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        return if (persian) {
            when {
                p.days > 0 ->
                    "${digits(p.days, true)} روز و ${digits(p.hours, true)} ساعت مانده"
                p.hours > 0 ->
                    "${digits(p.hours, true)} ساعت و ${digits(p.minutes, true)} دقیقه مانده"
                p.minutes > 0 ->
                    "${digits(p.minutes, true)} دقیقه و ${digits(p.seconds, true)} ثانیه مانده"
                else ->
                    "${digits(p.seconds, true)} ثانیه مانده"
            }
        } else {
            when {
                p.days > 0 ->
                    "${digits(p.days, false)} days, ${digits(p.hours, false)} hours left"
                p.hours > 0 ->
                    "${digits(p.hours, false)} hours, ${digits(p.minutes, false)} minutes left"
                p.minutes > 0 ->
                    "${digits(p.minutes, false)} minutes, ${digits(p.seconds, false)} seconds left"
                else ->
                    "${digits(p.seconds, false)} seconds left"
            }
        }
    }

    fun unitValues(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): List<String> {
        val p = parts(dueAtMillis, now)
        if (p.overdue) {
            val zero = digits("0", persian)
            return listOf(zero, zero, zero, zero)
        }

        return listOf(
            digits(p.days, persian),
            pad2(p.hours, persian),
            pad2(p.minutes, persian),
            pad2(p.seconds, persian)
        )
    }

    fun unitLabels(persian: Boolean): List<String> =
        if (persian) {
            listOf("روز", "ساعت", "دقیقه", "ثانیه")
        } else {
            listOf("Days", "Hours", "Minutes", "Seconds")
        }

    fun segments(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        val items = buildList {
            if (p.days > 0) {
                add(unit(p.days, persian, "روز", "days"))
            }
            if (p.hours > 0) {
                add(unit(p.hours, persian, "ساعت", "hr"))
            }
            if (p.minutes > 0) {
                add(unit(p.minutes, persian, "دقیقه", "min"))
            }
            if (p.seconds > 0 && size < 3) {
                add(unit(p.seconds, persian, "ثانیه", "sec"))
            }
        }

        return if (items.isEmpty()) {
            if (persian) "همین حالا" else "Now"
        } else {
            items.take(3).joinToString(" • ")
        }
    }

    fun focusPrimary(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        return when {
            p.days > 0 ->
                unit(p.days, persian, "روز", "days")
            p.hours > 0 ->
                unit(p.hours, persian, "ساعت", "hours")
            p.minutes > 0 ->
                unit(p.minutes, persian, "دقیقه", "minutes")
            else ->
                unit(p.seconds, persian, "ثانیه", "seconds")
        }
    }

    fun focusSecondary(
        dueAtMillis: Long,
        now: Long,
        persian: Boolean
    ): String {
        val p = parts(dueAtMillis, now)
        if (p.overdue) return overdueLabel(persian)

        val detail = when {
            p.days > 0 -> listOf(
                unit(p.hours, persian, "ساعت", "hr"),
                unit(p.minutes, persian, "دقیقه", "min")
            )
            p.hours > 0 -> listOf(
                unit(p.minutes, persian, "دقیقه", "min"),
                unit(p.seconds, persian, "ثانیه", "sec")
            )
            p.minutes > 0 -> listOf(
                unit(p.seconds, persian, "ثانیه", "sec")
            )
            else -> emptyList()
        }.filter {
            !it.startsWith("0") && !it.startsWith("۰")
        }

        return if (detail.isEmpty()) {
            if (persian) "به‌زودی" else "Soon"
        } else {
            if (persian) {
                detail.joinToString(" • ") + " " + leftLabel(true)
            } else {
                detail.joinToString(" • ") + " " + leftLabel(false)
            }
        }
    }
}
