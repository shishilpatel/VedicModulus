package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientProfileDao {
    @Query("SELECT * FROM client_profiles WHERE astrologerEmail = :astrologerEmail ORDER BY createdAt DESC")
    fun getClientsForAstrologerFlow(astrologerEmail: String): Flow<List<ClientProfileEntity>>

    @Query("SELECT * FROM client_profiles WHERE id = :clientId LIMIT 1")
    suspend fun getClientById(clientId: Long): ClientProfileEntity?

    @Query("SELECT * FROM client_profiles WHERE astrologerEmail = :astrologerEmail AND (fullName LIKE '%' || :query || '%' OR cityName LIKE '%' || :query || '%' OR phoneOrEmail LIKE '%' || :query || '%') ORDER BY fullName ASC")
    fun searchClientsFlow(astrologerEmail: String, query: String): Flow<List<ClientProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientProfileEntity): Long

    @Update
    suspend fun updateClient(client: ClientProfileEntity)

    @Query("DELETE FROM client_profiles WHERE id = :clientId")
    suspend fun deleteClient(clientId: Long)
}
