package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HolidayCategory(val titleTr: String, val emoji: String) {
    ALL("Tümü", "✨"),
    NATIONAL("Resmi Tatil", "🇹🇷"),
    RELIGIOUS("Dini Bayram", "🌙"),
    SCHOOL("Okul & Sömestr", "🎒"),
    SPECIAL("Özel Günler", "🎉"),
    CUSTOM("Tatillerim", "✈️")
}

data class HolidayItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: HolidayCategory,
    val targetEpochMillis: Long,
    val durationDays: Double,
    val description: String,
    val bridgeTip: String? = null,
    val location: String? = null,
    val iconKey: String = "calendar",
    val accentColorHex: Long = 0xFF0284C7,
    val isCustom: Boolean = false
) {
    fun calculateCountdown(currentEpochMillis: Long): CountdownDetail {
        val diffMillis = targetEpochMillis - currentEpochMillis
        val durationMillis = (durationDays * 24 * 60 * 60 * 1000).toLong()

        // Check if holiday is happening right now (today / during duration)
        val isOngoing = currentEpochMillis in targetEpochMillis..(targetEpochMillis + durationMillis)
        val isPassed = currentEpochMillis > (targetEpochMillis + durationMillis)

        if (diffMillis <= 0 && isOngoing) {
            val remainingDuringHoliday = (targetEpochMillis + durationMillis) - currentEpochMillis
            val totalSeconds = (remainingDuringHoliday / 1000).coerceAtLeast(0)
            val days = totalSeconds / (24 * 3600)
            val hours = (totalSeconds % (24 * 3600)) / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return CountdownDetail(
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                totalSeconds = totalSeconds,
                isPassed = false,
                isToday = true,
                formattedDate = formatDate(targetEpochMillis),
                formattedDayOfWeek = formatDayOfWeek(targetEpochMillis),
                progress = 1f
            )
        }

        if (diffMillis <= 0) {
            return CountdownDetail(
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                totalSeconds = 0,
                isPassed = true,
                isToday = false,
                formattedDate = formatDate(targetEpochMillis),
                formattedDayOfWeek = formatDayOfWeek(targetEpochMillis),
                progress = 1f
            )
        }

        val totalSeconds = diffMillis / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        // Progress within a typical 90-day window or 365-day year
        val maxDaysWindow = 120.0
        val progress = (1.0 - (days.toDouble() / maxDaysWindow)).coerceIn(0.0, 1.0).toFloat()

        return CountdownDetail(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalSeconds = totalSeconds,
            isPassed = false,
            isToday = false,
            formattedDate = formatDate(targetEpochMillis),
            formattedDayOfWeek = formatDayOfWeek(targetEpochMillis),
            progress = progress
        )
    }

    private fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
        return sdf.format(Date(epochMillis))
    }

    private fun formatDayOfWeek(epochMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale("tr"))
        return sdf.format(Date(epochMillis))
    }
}

data class CountdownDetail(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val isPassed: Boolean,
    val isToday: Boolean,
    val formattedDate: String,
    val formattedDayOfWeek: String,
    val progress: Float
)
