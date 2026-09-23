package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.util.Log

@Dao
interface UserProfileDao {
    companion object {
        private const val TAG = "UserProfileDao"
    }

    @Query("SELECT * FROM user_profiles ORDER BY isPrimary DESC, createdAt ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfileEntity?

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long {
        return try {
            Log.d(TAG, "Inserting profile transaction started for: ${profile.fullName}")
            val id = insertProfileInternal(profile)
            Log.d(TAG, "Profile successfully inserted with ID: $id and transaction committed.")
            id
        } catch (e: Exception) {
            Log.e(TAG, "ERROR in insertProfile transaction for ${profile.fullName}: ${e.message}", e)
            throw e
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileInternal(profile: UserProfileEntity): Long

    @Transaction
    @Update
    suspend fun updateProfile(profile: UserProfileEntity) {
        try {
            Log.d(TAG, "Updating profile transaction started for ID: ${profile.id}")
            updateProfileInternal(profile)
            Log.d(TAG, "Profile successfully updated and transaction committed.")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR in updateProfile transaction for ID ${profile.id}: ${e.message}", e)
            throw e
        }
    }

    @Update
    suspend fun updateProfileInternal(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :profileId AND isPrimary = 0")
    suspend fun deleteProfile(profileId: Long)
}
