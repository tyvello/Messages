package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OtpDetails
import com.example.data.model.SmsCategory
import com.example.data.model.SmsMessageItem
import com.example.ui.components.CategoryBadge
import com.example.ui.components.OtpQuickActionCard
import com.example.ui.components.PermissionBanner
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentEmeraldText
import com.example.ui.theme.AccentWarningContainer
import com.example.ui.theme.AccentWarningText
import com.example.ui.theme.AppOutlineLight
import com.example.ui.theme.AppSurfaceVariantLight
import com.example.ui.theme.AppWhite
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeBorder
import com.example.ui.theme.PrimaryOrangeContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InboxScreen(
    messages: List<SmsMessageItem>,
    latestOtp: OtpDetails?,
    searchQuery: String,
    selectedCategory: SmsCategory,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (SmsCategory) -> Unit,
    onRefresh: () -> Unit,
    onCopyOtp: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val quickSearchTags = listOf("OTP", "Google", "Bank", "Amazon", "Uber", "Security")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Permission Card
        item {
            PermissionBanner(
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission
            )
        }

        // Active OTP Quick Action Card (Copy-focused)
        if (latestOtp != null && searchQuery.isBlank()) {
            item {
                OtpQuickActionCard(
                    otp = latestOtp,
                    onCopy = { onCopyOtp(latestOtp.code) }
                )
            }
        }

        // Beautiful Modern Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSearching = searchQuery.isNotBlank()
                val borderColor by animateColorAsState(
                    targetValue = if (isSearchFocused || isSearching) PrimaryOrange else Color(0xFFE8E0D9),
                    label = "searchBorderColor"
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppWhite,
                    border = BorderStroke(if (isSearchFocused || isSearching) 1.5.dp else 1.dp, borderColor),
                    shadowElevation = if (isSearchFocused) 2.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Search Icon with subtle animated badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSearching || isSearchFocused) PrimaryOrangeContainer else Color(0xFFF6F3F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearching || isSearchFocused) PrimaryOrange else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Custom Search Text Field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search OTPs, senders, banking...",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isSearchFocused = it.isFocused }
                                    .testTag("sms_search_input"),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(PrimaryOrange),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                            )
                        }

                        // Right Action: Match Count Pill & Clear Button
                        AnimatedVisibility(
                            visible = isSearching,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Match Count Pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (messages.isNotEmpty()) PrimaryOrangeContainer else Color(0xFFFFECEC)
                                ) {
                                    Text(
                                        text = "${messages.size} found",
                                        color = if (messages.isNotEmpty()) PrimaryOrange else Color(0xFFD32F2F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Clear Button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2ECE7))
                                        .clickable {
                                            onSearchChange("")
                                            focusManager.clearFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Search Suggestion Tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickSearchTags) { tag ->
                        val isTagActive = searchQuery.equals(tag, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isTagActive) PrimaryOrange else Color(0xFFF5F0EB),
                            border = BorderStroke(
                                1.dp,
                                if (isTagActive) PrimaryOrange else Color(0xFFE5DDD6)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isTagActive) {
                                        onSearchChange("")
                                    } else {
                                        onSearchChange(tag)
                                    }
                                }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.5.sp,
                                fontWeight = if (isTagActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTagActive) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                items(SmsCategory.entries.toTypedArray()) { category ->
                    val isSelected = category == selectedCategory
                    val label = when (category) {
                        SmsCategory.ALL -> "All Messages"
                        SmsCategory.OTP -> "Passcodes (OTP)"
                        SmsCategory.BANKING -> "Banking"
                        SmsCategory.SECURITY -> "Security"
                        SmsCategory.GENERAL -> "General"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                label,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange,
                            selectedLabelColor = Color.White,
                            containerColor = AppWhite,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryOrange else AppOutlineLight
                        )
                    )
                }
            }
        }

        // Active Search Status Banner (If search is active)
        if (searchQuery.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF9F5),
                    border = BorderStroke(1.dp, PrimaryOrangeBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Results for ")
                                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryOrange))
                                    append("\"$searchQuery\"")
                                    pop()
                                },
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSearchChange("") }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Inbox Header / Count & Refresh
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else "Messages",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = PrimaryOrangeContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${messages.size}",
                            color = PrimaryOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PrimaryOrangeContainer)
                        .testTag("refresh_inbox_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh inbox",
                        tint = PrimaryOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Empty State or Messages
        if (messages.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    border = BorderStroke(1.dp, AppOutlineLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isSearchEmpty = searchQuery.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isSearchEmpty) Color(0xFFFFF0E6) else PrimaryOrangeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSearchEmpty) Icons.Outlined.SearchOff else Icons.Outlined.MailOutline,
                                    contentDescription = "Empty",
                                    tint = PrimaryOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isSearchEmpty) "No Matches for \"$searchQuery\"" else "No SMS Messages Detected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSearchEmpty) "We couldn't find any message matching your search query. Try typing another keyword or selecting a different category."
                            else if (!hasPermission) "Grant SMS permissions above to enable real-time message listening and automatic passcode extraction."
                            else "Incoming SMS messages with OTP codes will be captured and displayed here automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (isSearchEmpty) {
                            Button(
                                onClick = { onSearchChange("") },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Search", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else if (!hasPermission) {
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enable SMS Access", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onRefresh,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PrimaryOrange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = PrimaryOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Device Inbox", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(messages, key = { it.id }) { message ->
                SmsMessageCard(
                    message = message,
                    searchQuery = searchQuery,
                    onCopyCode = { code -> onCopyOtp(code) },
                    onDelete = { onDeleteMessage(message.id) }
                )
            }
        }
    }
}

/**
 * Helper to highlight matching search queries inside text
 */
fun highlightSearchText(
    fullText: String,
    query: String,
    highlightColor: Color = Color(0xFFFFD1A9),
    highlightTextColor: Color = Color(0xFF993300)
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(fullText)
    val lowerText = fullText.lowercase()
    val lowerQuery = query.trim().lowercase()
    val builder = AnnotatedString.Builder()
    var currentIndex = 0

    while (currentIndex < fullText.length) {
        val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
        if (matchIndex == -1) {
            builder.append(fullText.substring(currentIndex))
            break
        }
        if (matchIndex > currentIndex) {
            builder.append(fullText.substring(currentIndex, matchIndex))
        }
        val endIndex = matchIndex + lowerQuery.length
        builder.pushStyle(
            SpanStyle(
                background = highlightColor,
                color = highlightTextColor,
                fontWeight = FontWeight.Bold
            )
        )
        builder.append(fullText.substring(matchIndex, endIndex))
        builder.pop()
        currentIndex = endIndex
    }
    return builder.toAnnotatedString()
}

@Composable
fun SmsMessageCard(
    message: SmsMessageItem,
    searchQuery: String = "",
    onCopyCode: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeAgo = remember(message.timestamp) {
        DateUtils.getRelativeTimeSpanString(
            message.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    val isOtp = message.otpDetails != null
    val cardShape = RoundedCornerShape(18.dp)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isOtp) Color(0xFFFFFDFB) else AppWhite
        ),
        border = BorderStroke(
            1.dp,
            if (isOtp) PrimaryOrangeBorder else AppOutlineLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOtp) 2.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
            .testTag("sms_card_${message.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Sender & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (message.category) {
                                    SmsCategory.OTP -> PrimaryOrangeContainer
                                    SmsCategory.BANKING -> AccentEmeraldContainer
                                    SmsCategory.SECURITY -> AccentWarningContainer
                                    else -> AppSurfaceVariantLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.sender.take(2).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = when (message.category) {
                                SmsCategory.OTP -> PrimaryOrange
                                SmsCategory.BANKING -> AccentEmeraldText
                                SmsCategory.SECURITY -> AccentWarningText
                                else -> TextSecondary
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = highlightSearchText(message.sender, searchQuery),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = timeAgo,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                CategoryBadge(category = message.category)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text with Search Query Highlighting
            Text(
                text = highlightSearchText(message.body, searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // OTP Highlight Section if parsed
            if (message.otpDetails != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = PrimaryOrangeContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryOrangeBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "OTP Code",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PASSCODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    color = PrimaryOrange
                                )
                                Text(
                                    text = highlightSearchText(
                                        message.otpDetails.code,
                                        searchQuery,
                                        highlightColor = Color(0xFFFFCC99),
                                        highlightTextColor = PrimaryOrange
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { onCopyCode(message.otpDetails.code) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Expandable bottom actions
            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Message", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

