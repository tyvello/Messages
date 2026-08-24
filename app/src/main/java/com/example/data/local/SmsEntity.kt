package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.OtpDetails
import com.example.data.model.SmsCategory
import com.example.data.model.SmsMessageItem

@Entity(tableName = "sms_messages")
data class SmsEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val otpCode: String? = null,
    val serviceName: String? = null,
    val expiresAt: Long? = null,
    val rawContext: String? = null,
    val category: String = SmsCategory.GENERAL.name
) {
    fun toDomainModel(): SmsMessageItem {
        val otp = if (!otpCode.isNullOrBlank() && !serviceName.isNullOrBlank()) {
            OtpDetails(
                code = otpCode,
                serviceName = serviceName,
                expiresAt = expiresAt,
                rawContext = rawContext ?: body
            )
        } else null

        val cat = try {
            SmsCategory.valueOf(category)
        } catch (_: Exception) {
            SmsCategory.GENERAL
        }

        return SmsMessageItem(
            id = id,
            sender = sender,
            body = body,
            timestamp = timestamp,
            isRead = isRead,
            otpDetails = otp,
            category = cat
        )
    }

    companion object {
        fun fromDomainModel(item: SmsMessageItem): SmsEntity {
            return SmsEntity(
                id = item.id,
                sender = item.sender,
                body = item.body,
                timestamp = item.timestamp,
                isRead = item.isRead,
                otpCode = item.otpDetails?.code,
                serviceName = item.otpDetails?.serviceName,
                expiresAt = item.otpDetails?.expiresAt,
                rawContext = item.otpDetails?.rawContext,
                category = item.category.name
            )
        }
    }
}
