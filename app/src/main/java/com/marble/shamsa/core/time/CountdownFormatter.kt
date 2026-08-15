package com.marble.shamsa.core.time

import kotlin.math.abs

data class CountdownParts(val days: Long, val hours: Long, val minutes: Long, val seconds: Long, val overdue: Boolean)

object CountdownFormatter {
    fun parts(dueAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): CountdownParts {
        var total = (dueAtMillis - nowMillis) / 1000L
        val overdue = total < 0
        total = abs(total)
        val days = total / 86_400
        total %= 86_400
        val hours = total / 3_600
        total %= 3_600
        val minutes = total / 60
        val seconds = total % 60
        return CountdownParts(days, hours, minutes, seconds, overdue)
    }

    fun compact(dueAtMillis: Long, nowMillis: Long = System.currentTimeMillis(), persian: Boolean = false): String {
        val p = parts(dueAtMillis, nowMillis)
        val body = when {
            p.days > 0 -> if (persian) "${p.days} روز ${p.hours} ساعت" else "${p.days}d ${p.hours}h"
            p.hours > 0 -> if (persian) "${p.hours} ساعت ${p.minutes} دقیقه" else "${p.hours}h ${p.minutes}m"
            else -> if (persian) "%02d:%02d".format(p.minutes, p.seconds) else "%02d:%02d".format(p.minutes, p.seconds)
        }
        return if (p.overdue) (if (persian) "$body گذشته" else "$body ago") else body
    }
}
