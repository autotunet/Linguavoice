package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDayDao {

    @Query("SELECT * FROM practice_days ORDER BY dateString DESC")
    fun getAllPracticeDays(): Flow<List<PracticeDayEntity>>

    @Query("SELECT * FROM practice_days WHERE dateString = :dateString LIMIT 1")
    suspend fun getPracticeDayByDate(dateString: String): PracticeDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(practiceDay: PracticeDayEntity)

    @Query("SELECT COUNT(*) FROM practice_days")
    fun getTotalDaysPracticed(): Flow<Int>

    @Query("DELETE FROM practice_days")
    suspend fun clearAll()
}
