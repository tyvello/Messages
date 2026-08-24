package com.example.data.model

data class SmsMessageItem(
    val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val otpDetails: OtpDetails? = null,
    val category: SmsCategory = SmsCategory.GENERAL
)

data class SentMessageItem(
    val id: String,
    val recipient: String,
    val body: String,
    val timestamp: Long,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.DELIVERED
)

enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

enum class SmsCategory {
    ALL,
    OTP,
    BANKING,
    SECURITY,
    GENERAL
}

data class OtpDetails(
    val code: String,
    val serviceName: String,
    val expiresAt: Long? = null,
    val rawContext: String = ""
)
