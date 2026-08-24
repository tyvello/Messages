package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SmsApplication
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.OtpDetails
import com.example.data.model.SentMessageItem
import com.example.data.model.SmsCategory
import com.example.data.model.SmsMessageItem
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    INBOX,
    SENT
}

class SmsViewModel(
    private val repository: SmsRepository = SmsApplication.instance.repository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AppTab.INBOX)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(SmsCategory.ALL)
    val selectedCategory: StateFlow<SmsCategory> = _selectedCategory.asStateFlow()

    private val _snackBarEvent = MutableSharedFlow<String>()
    val snackBarEvent: SharedFlow<String> = _snackBarEvent.asSharedFlow()

    val latestOtp: StateFlow<OtpDetails?> = repository.latestOtp

    val filteredMessages: StateFlow<List<SmsMessageItem>> = combine(
        repository.messages,
        _searchQuery,
        _selectedCategory
    ) { messages, query, category ->
        messages
            .distinctBy { "${it.sender.trim().lowercase()}:::${it.body.trim()}" }
            .filter { item ->
                val matchesCategory = when (category) {
                    SmsCategory.ALL -> true
                    else -> item.category == category
                }
                val matchesQuery = if (query.isBlank()) true else {
                    item.sender.contains(query, ignoreCase = true) ||
                            item.body.contains(query, ignoreCase = true) ||
                            (item.otpDetails?.code?.contains(query, ignoreCase = true) == true) ||
                            (item.otpDetails?.serviceName?.contains(query, ignoreCase = true) == true)
                }
                matchesCategory && matchesQuery
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentMessages: StateFlow<List<SentMessageItem>> = repository.sentMessages

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: SmsCategory) {
        _selectedCategory.value = category
    }

    fun refreshMessages() {
        repository.syncDeviceInbox()
        repository.syncDeviceSentMessages()
        postSnackbar("Synced SMS messages")
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Passcode") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        postSnackbar("Copied '$text' to clipboard")
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            repository.deleteMessage(id)
            postSnackbar("Message removed")
        }
    }

    fun deleteSentMessage(id: String) {
        viewModelScope.launch {
            repository.deleteSentMessage(id)
            postSnackbar("Sent message removed")
        }
    }

    fun sendRealSms(destinationNumber: String, messageText: String) {
        val cleanedNumber = destinationNumber.trim()
        val cleanedText = messageText.trim()

        if (cleanedNumber.isBlank() || cleanedText.isBlank()) {
            postSnackbar("Please enter a valid phone number and message")
            return
        }

        viewModelScope.launch {
            try {
                val smsManager = SmsManager.getDefault()
                val parts = smsManager.divideMessage(cleanedText)
                smsManager.sendMultipartTextMessage(cleanedNumber, null, parts, null, null)
                repository.recordSentMessage(
                    recipient = cleanedNumber,
                    body = cleanedText,
                    timestamp = System.currentTimeMillis(),
                    status = MessageDeliveryStatus.DELIVERED
                )
                postSnackbar("Message sent to $cleanedNumber")
            } catch (e: Exception) {
                // If carrier permission failed or error occurred, still record failed or show alert
                repository.recordSentMessage(
                    recipient = cleanedNumber,
                    body = cleanedText,
                    timestamp = System.currentTimeMillis(),
                    status = MessageDeliveryStatus.FAILED
                )
                postSnackbar("Failed to dispatch: ${e.localizedMessage ?: "Check SIM & permissions"}")
            }
        }
    }

    fun postSnackbar(msg: String) {
        viewModelScope.launch {
            _snackBarEvent.emit(msg)
        }
    }
}
