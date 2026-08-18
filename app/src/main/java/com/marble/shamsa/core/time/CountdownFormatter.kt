package com.marble.shamsa.core.time

import kotlin.math.abs

data class CountdownParts(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val overdue: Boolean
)

object CountdownFormatter {
    fun parts(dueAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): CountdownParts {
        val rawMillis = dueAtMillis - nowMillis
        var total = abs(rawMillis) / 1000L
        val overdue = rawMillis < 0L
        val days = total / 86_400
        total %= 86_400
        val hours = total / 3_600
        total %= 3_600
        val minutes = total / 60
        val seconds = total % 60
        return CountdownParts(days, hours, minutes, seconds, overdue)
    }

    fun compact(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        val body = when {
            p.days > 0 -> if (persian) "${p.days} روز ${p.hours} ساعت" else "${p.days}d ${p.hours}h"
            p.hours > 0 -> if (persian) "${p.hours} ساعت ${p.minutes} دقیقه" else "${p.hours}h ${p.minutes}m"
            else -> "%02d:%02d".format(p.minutes, p.seconds)
        }
        return localized(
            if (p.overdue) {
                if (persian) "$body گذشته" else "$body ago"
            } else body,
            persian
        )
    }

    fun digital(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        val body = if (p.days > 0) {
            "%02d:%02d:%02d:%02d".format(p.days, p.hours, p.minutes, p.seconds)
        } else {
            "%02d:%02d:%02d".format(p.hours, p.minutes, p.seconds)
        }
        return localized(if (p.overdue) "− $body" else body, persian)
    }

    fun digitalLegend(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        return if (p.days > 0) {
            if (persian) "روز : ساعت : دقیقه : ثانیه" else "DAY : HOUR : MIN : SEC"
        } else {
            if (persian) "ساعت : دقیقه : ثانیه" else "HOUR : MIN : SEC"
        }
    }

    fun segments(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        val body = if (persian) {
            "${pad(p.days)}ر • ${pad(p.hours)}س • ${pad(p.minutes)}د • ${pad(p.seconds)}ث"
        } else {
            "${pad(p.days)}d • ${pad(p.hours)}h • ${pad(p.minutes)}m • ${pad(p.seconds)}s"
        }
        return localized(if (p.overdue) "− $body" else body, persian)
    }

    fun focusPrimary(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        val text = when {
            p.days > 0 -> if (persian) "${p.days} روز" else "${p.days} days"
            p.hours > 0 -> if (persian) "${p.hours} ساعت" else "${p.hours} hours"
            p.minutes > 0 -> if (persian) "${p.minutes} دقیقه" else "${p.minutes} min"
            else -> if (persian) "${p.seconds} ثانیه" else "${p.seconds} sec"
        }
        return localized(if (p.overdue) "− $text" else text, persian)
    }

    fun focusSecondary(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): String {
        val p = parts(dueAtMillis, nowMillis)
        val body = when {
            p.days > 0 ->
                if (persian) "${pad(p.hours)} ساعت • ${pad(p.minutes)} دقیقه • ${pad(p.seconds)} ثانیه"
                else "${pad(p.hours)}h • ${pad(p.minutes)}m • ${pad(p.seconds)}s"
            p.hours > 0 ->
                if (persian) "${pad(p.minutes)} دقیقه • ${pad(p.seconds)} ثانیه"
                else "${pad(p.minutes)}m • ${pad(p.seconds)}s"
            else -> if (persian) "${pad(p.seconds)} ثانیه" else "${pad(p.seconds)}s"
        }
        return localized(
            if (p.overdue) {
                if (persian) "$body از موعد گذشته" else "$body overdue"
            } else body,
            persian
        )
    }

    fun unitValues(
        dueAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        persian: Boolean = false
    ): List<String> {
        val p = parts(dueAtMillis, nowMillis)
        return listOf(p.days, p.hours, p.minutes, p.seconds)
            .map { localized(pad(it), persian) }
    }

    fun unitLabels(persian: Boolean): List<String> =
        if (persian) listOf("روز", "ساعت", "دقیقه", "ثانیه")
        else listOf("DAYS", "HRS", "MIN", "SEC")

    private fun pad(value: Long): String = value.toString().padStart(2, '0')

    private fun localized(value: String, persian: Boolean): String {
        if (!persian) return value
        val en = "0123456789"
        val fa = "۰۱۲۳۴۵۶۷۸۹"
        return buildString(value.length) {
            value.forEach { ch ->
                val index = en.indexOf(ch)
                append(if (index >= 0) fa[index] else ch)
            }
        }
    }
}
