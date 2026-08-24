package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.SentMessageItem
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sent_messages")
data class SentMessageEntity(
    @PrimaryKey val id: String,
    val recipient: String,
    val body: String,
    val timestamp: Long,
    val status: String = MessageDeliveryStatus.DELIVERED.name
) {
    fun toDomainModel(): SentMessageItem {
        val deliveryStatus = try {
            MessageDeliveryStatus.valueOf(status)
        } catch (_: Exception) {
            MessageDeliveryStatus.DELIVERED
        }
        return SentMessageItem(
            id = id,
            recipient = recipient,
            body = body,
            timestamp = timestamp,
            status = deliveryStatus
        )
    }

    companion object {
        fun fromDomainModel(item: SentMessageItem): SentMessageEntity {
            return SentMessageEntity(
                id = item.id,
                recipient = item.recipient,
                body = item.body,
                timestamp = item.timestamp,
                status = item.status.name
            )
        }
    }
}

@Dao
interface SentMessageDao {
    @Query("SELECT * FROM sent_messages ORDER BY timestamp DESC")
    fun getAllSentMessages(): Flow<List<SentMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentMessage(message: SentMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SentMessageEntity>)

    @Query("DELETE FROM sent_messages WHERE id = :id")
    suspend fun deleteSentMessage(id: String)

    @Query("DELETE FROM sent_messages")
    suspend fun clearAll()
}
