package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.SentMessageItem
import com.example.ui.theme.AccentError
import com.example.ui.theme.AppOutlineLight
import com.example.ui.theme.AppWhite
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeContainer
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SentScreen(
    sentMessages: List<SentMessageItem>,
    onSendSms: (recipient: String, body: String) -> Unit,
    onDeleteSentMessage: (String) -> Unit,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var recipientPhone by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var isRecipientFocused by remember { mutableStateOf(false) }
    var isMessageFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val messageFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Smart Quick Templates
    val quickTemplates = listOf(
        "Code received, thanks!",
        "Please call me back.",
        "Confirmed & approved.",
        "Got it!",
        "I'll be there in 5 mins."
    )

    // Recent recipients extracted from sent history
    val recentRecipients = remember(sentMessages) {
        sentMessages
            .map { it.recipient.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
    }

    // Filter sent messages by search query
    val filteredMessages = remember(sentMessages, searchQuery) {
        if (searchQuery.isBlank()) {
            sentMessages
        } else {
            val q = searchQuery.trim().lowercase()
            sentMessages.filter {
                it.recipient.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }
    }

    // Calculation for SMS standard segments (160 chars per SMS)
    val charCount = messageText.length
    val smsSegments = if (charCount == 0) 1 else (charCount - 1) / 160 + 1
    val canSend = recipientPhone.trim().isNotBlank() && messageText.trim().isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F5))
            .imePadding()
    ) {
        // Top Outbound Header & Search Bar
        Surface(
            color = AppWhite,
            shadowElevation = 0.5.dp,
            border = BorderStroke(1.dp, Color(0xFFEFE8E2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrangeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sent_message),
                                contentDescription = "Sent",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Outbound Dispatch",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PrimaryOrangeContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${sentMessages.size} Sent",
                                color = PrimaryOrange,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Search Toggle Button
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search Outbound",
                                tint = if (isSearchExpanded || searchQuery.isNotEmpty()) PrimaryOrange else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expandable Search Bar in Sent Screen
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF7F3EE),
                        border = BorderStroke(1.dp, Color(0xFFE8E0D8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(PrimaryOrange),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search sent recipients or text...",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sent Messages History Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                item {
                    val isSearchEmpty = searchQuery.isNotBlank()
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppWhite),
                        border = BorderStroke(1.dp, AppOutlineLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isSearchEmpty) Color(0xFFFFF0E6) else PrimaryOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSearchEmpty) Icons.Outlined.SearchOff else Icons.Default.Send,
                                    contentDescription = "Sent",
                                    tint = PrimaryOrange,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isSearchEmpty) "No Matches for \"$searchQuery\"" else "No Sent Messages Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isSearchEmpty) "Try searching with a different phone number or keyword."
                                else "Compose and dispatch outbound carrier SMS directly from the smart compose bar below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                            if (isSearchEmpty) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear Search", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredMessages, key = { it.id }) { msg ->
                    SentMessageBubbleItem(
                        message = msg,
                        searchQuery = searchQuery,
                        onDelete = { onDeleteSentMessage(msg.id) },
                        onCopy = { onCopyText(msg.body) },
                        onResend = {
                            recipientPhone = msg.recipient
                            messageText = msg.body
                            coroutineScope.launch {
                                messageFocusRequester.requestFocus()
                            }
                        }
                    )
                }
            }
        }

        // ==========================================
        // SMART COMPOSER DOCK (Bottom Section)
        // ==========================================
        Surface(
            color = AppWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, Color(0xFFE8E1DA)),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Quick Recent Recipients Pill Row (If available)
                if (recentRecipients.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(recentRecipients) { rec ->
                                val isSelected = recipientPhone == rec
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryOrangeContainer else Color(0xFFF7F3EE),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryOrange else Color(0xFFE5DDD5)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            recipientPhone = rec
                                            coroutineScope.launch {
                                                messageFocusRequester.requestFocus()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryOrange else TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = rec,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PrimaryOrange else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Recipient Input Bar
                val recipientBorderColor by animateColorAsState(
                    targetValue = if (isRecipientFocused) PrimaryOrange else Color(0xFFE5DDD5),
                    label = "recipientBorder"
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(if (isRecipientFocused) 1.5.dp else 1.dp, recipientBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryOrangeContainer
                        ) {
                            Text(
                                text = "TO",
                                color = PrimaryOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        BasicTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isRecipientFocused = it.isFocused }
                                .testTag("sent_recipient_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(PrimaryOrange),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { coroutineScope.launch { messageFocusRequester.requestFocus() } }
                            ),
                            decorationBox = { innerTextField ->
                                if (recipientPhone.isEmpty()) {
                                    Text(
                                        text = "Phone number or recipient...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (recipientPhone.isNotEmpty()) {
                            IconButton(
                                onClick = { recipientPhone = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear recipient",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Quick Message Templates (One-tap insertion)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 1.dp)
                ) {
                    items(quickTemplates) { template ->
                        Surface(
                            color = Color(0xFFF6F2ED),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5DDD5)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    messageText = if (messageText.isBlank()) template else "$messageText $template"
                                }
                        ) {
                            Text(
                                text = template,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 4. Main Compose Area + Character Count + Big Send Button
                val messageBorderColor by animateColorAsState(
                    targetValue = if (isMessageFocused) PrimaryOrange else Color(0xFFE5DDD5),
                    label = "messageBorder"
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(if (isMessageFocused) 1.5.dp else 1.dp, messageBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Multi-line Expanding Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            ) {
                                if (messageText.isEmpty()) {
                                    Text(
                                        text = "Type your message here...",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(messageFocusRequester)
                                        .onFocusChanged { isMessageFocused = it.isFocused }
                                        .testTag("sent_message_input"),
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 5,
                                    cursorBrush = SolidColor(PrimaryOrange),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Send Button with animated feedback
                            Surface(
                                shape = CircleShape,
                                color = if (canSend) PrimaryOrange else Color(0xFFE8E2DC),
                                shadowElevation = if (canSend) 2.dp else 0.dp,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canSend) {
                                        if (canSend) {
                                            onSendSms(recipientPhone, messageText)
                                            messageText = ""
                                            focusManager.clearFocus()
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(0)
                                            }
                                        }
                                    }
                                    .testTag("send_action_btn")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (canSend) PrimaryOrangeGradient
                                            else Brush.linearGradient(listOf(Color(0xFFE8E2DC), Color(0xFFDFD8D1)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_sent_message),
                                        contentDescription = "Send SMS",
                                        tint = if (canSend) Color.White else Color(0xFFA09992),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Bottom Meta Row: Character Counter & Segment info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (charCount > 0) {
                                Text(
                                    text = "$charCount / 160 • $smsSegments SMS",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (charCount > 160) PrimaryOrange else TextMuted
                                )
                            } else {
                                Text(
                                    text = "Carrier standard SMS",
                                    fontSize = 10.5.sp,
                                    color = TextMuted
                                )
                            }

                            if (messageText.isNotEmpty()) {
                                Text(
                                    text = "Clear Text",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { messageText = "" }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentMessageBubbleItem(
    message: SentMessageItem,
    searchQuery: String = "",
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a • MMM d", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val initials = remember(message.recipient) {
        val cleaned = message.recipient.filter { it.isLetterOrDigit() }
        if (cleaned.length >= 2) cleaned.take(2).uppercase() else "TO"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Recipient header tag with avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp, end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryOrange
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "To: ${message.recipient}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontSize = 11.5.sp
            )
        }

        // Modern Outbound Chat Bubble
        val bubbleShape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
        Surface(
            color = PrimaryOrange,
            shape = bubbleShape,
            shadowElevation = 0.dp,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .clickable { isExpanded = !isExpanded }
        ) {
            Column(
                modifier = Modifier
                    .background(PrimaryOrangeGradient)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = message.body,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageDeliveryStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageDeliveryStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageDeliveryStatus.SENDING -> {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sending",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageDeliveryStatus.FAILED -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Failed",
                                tint = Color(0xFFFFD1D1),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expanded Action Drawer (Copy, Resend, Delete)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, end = 2.dp)
            ) {
                Surface(
                    color = AppWhite,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5DDD5)),
                    modifier = Modifier.clickable { onCopy() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 10.5.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = AppWhite,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5DDD5)),
                    modifier = Modifier.clickable { onResend() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resend", fontSize = 10.5.sp, color = PrimaryOrange, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = AppWhite,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5DDD5)),
                    modifier = Modifier.clickable { onDelete() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = AccentError,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 10.5.sp, color = AccentError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
