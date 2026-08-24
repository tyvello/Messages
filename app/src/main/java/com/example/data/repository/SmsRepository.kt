package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.SentMessageEntity
import com.example.data.local.SmsEntity
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.OtpDetails
import com.example.data.model.SentMessageItem
import com.example.data.model.SmsMessageItem
import com.example.data.util.OtpParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SmsRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val smsDao = database.smsDao()
    private val sentMessageDao = database.sentMessageDao()

    val messages: StateFlow<List<SmsMessageItem>> = smsDao.getAllMessages()
        .map { entities ->
            entities
                .map { it.toDomainModel() }
                .distinctBy { "${it.sender.trim().lowercase()}:::${it.body.trim()}" }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sentMessages: StateFlow<List<SentMessageItem>> = sentMessageDao.getAllSentMessages()
        .map { entities ->
            entities
                .map { it.toDomainModel() }
                .distinctBy { "${it.recipient.trim().lowercase()}:::${it.body.trim()}" }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _latestOtp = MutableStateFlow<OtpDetails?>(null)
    val latestOtp: StateFlow<OtpDetails?> = _latestOtp.asStateFlow()

    init {
        scope.launch {
            try {
                smsDao.purgeDuplicateMessages()
            } catch (_: Exception) {}
        }
        scope.launch {
            messages.collect { list ->
                val firstOtp = list.firstOrNull { it.otpDetails != null }?.otpDetails
                _latestOtp.value = firstOtp
            }
        }
        syncDeviceInbox()
        syncDeviceSentMessages()
    }

    fun hasSmsPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun syncDeviceInbox() {
        if (!hasSmsPermissions()) return

        scope.launch {
            val deviceSms = readInboxSms()
            if (deviceSms.isNotEmpty()) {
                val uniqueMessages = deviceSms.distinctBy { "${it.sender.trim().lowercase()}:::${it.body.trim()}" }
                val entities = uniqueMessages.map { SmsEntity.fromDomainModel(it) }
                smsDao.insertAll(entities)
                try {
                    smsDao.purgeDuplicateMessages()
                } catch (_: Exception) {}
            }
        }
    }

    fun syncDeviceSentMessages() {
        if (!hasSmsPermissions()) return

        scope.launch {
            val deviceSent = readSentSms()
            if (deviceSent.isNotEmpty()) {
                val uniqueSent = deviceSent.distinctBy { "${it.recipient.trim().lowercase()}:::${it.body.trim()}" }
                val entities = uniqueSent.map { SentMessageEntity.fromDomainModel(it) }
                sentMessageDao.insertAll(entities)
            }
        }
    }

    private suspend fun readInboxSms(): List<SmsMessageItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<SmsMessageItem>()
        try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://sms/inbox"),
                arrayOf("_id", "address", "body", "date", "read"),
                null,
                null,
                "date DESC LIMIT 100"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex("_id")
                val addressIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")
                val readIdx = it.getColumnIndex("read")

                while (it.moveToNext()) {
                    val rawId = if (idIdx != -1) it.getString(idIdx) ?: "" else ""
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "Unknown" else "Unknown"
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                    val read = if (readIdx != -1) it.getInt(readIdx) == 1 else false

                    // Generate a stable unique ID based on sender and content to prevent duplicated entries across sync cycles
                    val stableKey = "${address.trim().lowercase()}:::${body.trim()}"
                    val id = if (rawId.isNotBlank()) "sms_$rawId" else "sms_key_${stableKey.hashCode()}"

                    val otp = OtpParser.parseOtp(address, body, date)
                    val category = OtpParser.categorize(address, body, otp)

                    list.add(
                        SmsMessageItem(
                            id = id,
                            sender = address,
                            body = body,
                            timestamp = date,
                            isRead = read,
                            otpDetails = otp,
                            category = category
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Handled
        }
        list.distinctBy { "${it.sender.trim().lowercase()}:::${it.body.trim()}" }
    }

    private suspend fun readSentSms(): List<SentMessageItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<SentMessageItem>()
        try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://sms/sent"),
                arrayOf("_id", "address", "body", "date"),
                null,
                null,
                "date DESC LIMIT 100"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex("_id")
                val addressIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val rawId = if (idIdx != -1) it.getString(idIdx) ?: "" else ""
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "Unknown" else "Unknown"
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    val stableKey = "${address.trim().lowercase()}:::${body.trim()}"
                    val id = if (rawId.isNotBlank()) "sent_$rawId" else "sent_key_${stableKey.hashCode()}"

                    list.add(
                        SentMessageItem(
                            id = id,
                            recipient = address,
                            body = body,
                            timestamp = date,
                            status = MessageDeliveryStatus.DELIVERED
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Handled
        }
        list.distinctBy { "${it.recipient.trim().lowercase()}:::${it.body.trim()}" }
    }

    suspend fun addIncomingMessage(
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis()
    ): SmsMessageItem = withContext(Dispatchers.IO) {
        val existingDuplicate = smsDao.findDuplicateMessage(sender, body)
        if (existingDuplicate != null) {
            val updated = existingDuplicate.copy(
                timestamp = maxOf(existingDuplicate.timestamp, timestamp)
            )
            smsDao.insertMessage(updated)
            val domain = updated.toDomainModel()
            if (domain.otpDetails != null) {
                _latestOtp.value = domain.otpDetails
            }
            return@withContext domain
        }

        val otp = OtpParser.parseOtp(sender, body, timestamp)
        val category = OtpParser.categorize(sender, body, otp)
        val stableKey = "${sender.trim().lowercase()}:::${body.trim()}"
        val item = SmsMessageItem(
            id = "sms_key_${stableKey.hashCode()}_${System.currentTimeMillis()}",
            sender = sender,
            body = body,
            timestamp = timestamp,
            isRead = false,
            otpDetails = otp,
            category = category
        )

        smsDao.insertMessage(SmsEntity.fromDomainModel(item))
        if (otp != null) {
            _latestOtp.value = otp
        }
        item
    }

    suspend fun recordSentMessage(
        recipient: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        status: MessageDeliveryStatus = MessageDeliveryStatus.DELIVERED
    ) = withContext(Dispatchers.IO) {
        val item = SentMessageItem(
            id = UUID.randomUUID().toString(),
            recipient = recipient,
            body = body,
            timestamp = timestamp,
            status = status
        )
        sentMessageDao.insertSentMessage(SentMessageEntity.fromDomainModel(item))
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        smsDao.deleteMessageById(id)
    }

    suspend fun deleteSentMessage(id: String) = withContext(Dispatchers.IO) {
        sentMessageDao.deleteSentMessage(id)
    }
}
