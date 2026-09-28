package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_holidays")
data class CustomHolidayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val targetEpochMillis: Long,
    val durationDays: Double = 1.0,
    val description: String = "",
    val location: String = "",
    val iconKey: String = "beach",
    val colorHex: Long = 0xFFD97706
)
