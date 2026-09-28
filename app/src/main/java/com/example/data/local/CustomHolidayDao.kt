package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomHolidayDao {
    @Query("SELECT * FROM custom_holidays ORDER BY targetEpochMillis ASC")
    fun getAllCustomHolidays(): Flow<List<CustomHolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomHoliday(holiday: CustomHolidayEntity): Long

    @Query("DELETE FROM custom_holidays WHERE id = :id")
    suspend fun deleteCustomHoliday(id: Long)
}
