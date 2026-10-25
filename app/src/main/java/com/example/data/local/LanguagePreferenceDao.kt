package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for reading and updating the user's target language preference.
 * Exposes reactive [Flow] queries and suspend write operations.
 */
@Dao
interface LanguagePreferenceDao {

    @Query("SELECT * FROM target_language_preferences WHERE id = :id LIMIT 1")
    fun observeTargetLanguagePreference(
        id: Int = LanguagePreferenceEntity.SINGLETON_ID
    ): Flow<LanguagePreferenceEntity?>

    @Query("SELECT * FROM target_language_preferences WHERE id = :id LIMIT 1")
    suspend fun getTargetLanguagePreference(
        id: Int = LanguagePreferenceEntity.SINGLETON_ID
    ): LanguagePreferenceEntity?

    @Query("SELECT * FROM target_language_preferences WHERE id = :id LIMIT 1")
    fun getTargetLanguagePreferenceSync(
        id: Int = LanguagePreferenceEntity.SINGLETON_ID
    ): LanguagePreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTargetLanguagePreference(preference: LanguagePreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertTargetLanguagePreferenceSync(preference: LanguagePreferenceEntity)

    @Query("DELETE FROM target_language_preferences")
    suspend fun clearAll()
}
