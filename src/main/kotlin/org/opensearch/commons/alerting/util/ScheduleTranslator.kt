/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.opensearch.commons.alerting.model.CronSchedule
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Schedule
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Translates OpenSearch [Schedule] (CronSchedule / IntervalSchedule) to
 * EventBridge Scheduler expression strings.
 *
 * EB rate: rate(value unit)  — unit is minute(s)|hour(s)|day(s)
 * EB cron: cron(min hour dom month dow year) — 6 fields, ? for mutual exclusion
 *
 * OpenSearch cron is standard Unix 5-field: min hour dom month dow
 * OpenSearch day-of-week: 0=Sun (cron-utils UNIX type)
 * EB day-of-week: 1=Sun..7=Sat
 */
object ScheduleTranslator {

    /**
     * Returns the EB ScheduleExpression string and optional timezone.
     * Timezone is only non-null for CronSchedule.
     */
    fun toEventBridgeExpression(schedule: Schedule): Pair<String, ZoneId?> {
        return when (schedule) {
            is IntervalSchedule -> Pair(translateInterval(schedule), null)
            is CronSchedule -> Pair(translateCron(schedule), schedule.timezone)
        }
    }

    private fun translateInterval(schedule: IntervalSchedule): String {
        val unit = when (schedule.unit) {
            ChronoUnit.SECONDS -> {
                if (schedule.interval < 60) {
                    throw IllegalArgumentException(
                        "EventBridge Scheduler does not support intervals less than 1 minute. " +
                            "Got ${schedule.interval} seconds."
                    )
                }
                val minutes = (schedule.interval + 59) / 60
                return "rate($minutes ${if (minutes == 1) "minute" else "minutes"})"
            }
            ChronoUnit.MINUTES -> if (schedule.interval == 1) "minute" else "minutes"
            ChronoUnit.HOURS -> if (schedule.interval == 1) "hour" else "hours"
            ChronoUnit.DAYS -> if (schedule.interval == 1) "day" else "days"
            else -> throw IllegalArgumentException("Unsupported interval unit: ${schedule.unit}")
        }
        return "rate(${schedule.interval} $unit)"
    }

    private fun translateCron(schedule: CronSchedule): String {
        val parts = schedule.expression.trim().split("\\s+".toRegex())
        require(parts.size == 5) { "Expected 5-field cron expression, got ${parts.size}: ${schedule.expression}" }

        val (min, hour, dom, month, dow) = parts
        val (ebDom, ebDow) = resolveDomDow(dom, translateDayOfWeek(dow))

        return "cron($min $hour $ebDom $month $ebDow *)"
    }

    /**
     * Translates Unix cron day-of-week to EB day-of-week.
     * Unix (cron-utils UNIX): 0=Sun, 1=Mon..6=Sat
     * EB Scheduler: 1=Sun, 2=Mon..7=Sat
     */
    private fun translateDayOfWeek(dow: String): String {
        if (dow == "*") return "*"

        return dow.split(",").joinToString(",") { part ->
            when {
                part.contains("-") -> {
                    val (start, end) = part.split("-", limit = 2)
                    "${unixDowToEb(start)}-${unixDowToEb(end)}"
                }
                part.contains("/") -> {
                    val (base, step) = part.split("/", limit = 2)
                    val ebBase = if (base == "*") "*" else unixDowToEb(base)
                    "$ebBase/$step"
                }
                else -> unixDowToEb(part)
            }
        }
    }

    private fun unixDowToEb(value: String): String {
        val num = value.toIntOrNull() ?: return value
        return ((num % 7) + 1).toString()
    }

    private fun resolveDomDow(dom: String, dow: String): Pair<String, String> {
        val domIsWild = dom == "*" || dom == "?"
        val dowIsWild = dow == "*" || dow == "?"
        return when {
            !domIsWild && !dowIsWild -> Pair(dom, "?")
            !domIsWild -> Pair(dom, "?")
            !dowIsWild -> Pair("?", dow)
            else -> Pair("*", "?")
        }
    }
}
