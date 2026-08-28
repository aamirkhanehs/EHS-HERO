package com.ehshero.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val displayFormatWithTime = SimpleDateFormat("MMM d, yyyy \u2022 h:mm a", Locale.getDefault())

    fun formatDate(date: Date?): String = date?.let { displayFormat.format(it) } ?: "-"

    fun formatDateTime(date: Date?): String = date?.let { displayFormatWithTime.format(it) } ?: "-"

    /** "3h ago", "2d ago", falling back to a plain date once it's more than
     * a week old - used on activity history and approval lists. */
    fun relativeTime(date: Date?, nowMillis: Long = System.currentTimeMillis()): String {
        if (date == null) return "-"
        val diffMs = nowMillis - date.time
        if (diffMs < 0) return formatDate(date)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> formatDate(date)
        }
    }
}
