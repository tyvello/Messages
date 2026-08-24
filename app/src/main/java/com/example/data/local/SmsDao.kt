package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SmsEntity>)

    @Query("SELECT * FROM sms_messages WHERE LOWER(TRIM(sender)) = LOWER(TRIM(:sender)) AND TRIM(body) = TRIM(:body) LIMIT 1")
    suspend fun findDuplicateMessage(sender: String, body: String): SmsEntity?

    @Query("DELETE FROM sms_messages WHERE id NOT IN (SELECT MIN(id) FROM sms_messages GROUP BY LOWER(TRIM(sender)), TRIM(body))")
    suspend fun purgeDuplicateMessages()

    @Query("UPDATE sms_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM sms_messages")
    suspend fun clearAll()
}
