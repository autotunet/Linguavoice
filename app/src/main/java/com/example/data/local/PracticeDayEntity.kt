package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_days")
data class PracticeDayEntity(
    @PrimaryKey
    val dateString: String, // format "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val exercisesCompleted: Int = 1,
    val xpEarned: Int = 15,
    val languageCode: String = "en",
    val accuracy: Int = 88
)
