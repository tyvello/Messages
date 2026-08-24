package com.example.data.util

import com.example.data.model.OtpDetails
import com.example.data.model.SmsCategory
import java.util.regex.Pattern

object OtpParser {

    private val OTP_PATTERNS = listOf(
        // Pattern with keywords like code, OTP, pin, verification
        Pattern.compile("(?i)(?:code|otp|pin|passcode|secret|verification|verify|is|key)[\\s:=-]+([0-9]{4,8})\\b"),
        // G-123456 or standard format
        Pattern.compile("(?i)\\b([A-Z]{1,3}-[0-9]{4,8})\\b"),
        // Standalone 4-8 digit number surrounded by word boundaries if contains verification keywords
        Pattern.compile("\\b([0-9]{4,8})\\b")
    )

    private val EXPIRY_PATTERN = Pattern.compile("(?i)(?:valid for|expires in|within)\\s+(\\d+)\\s*(min|minute|minutes|sec|second|seconds|hour|hours)?")

    fun parseOtp(sender: String, body: String, timestamp: Long): OtpDetails? {
        val lowerBody = body.lowercase()
        val isLikelyOtpMessage = lowerBody.contains("otp") ||
                lowerBody.contains("code") ||
                lowerBody.contains("verification") ||
                lowerBody.contains("verify") ||
                lowerBody.contains("security") ||
                lowerBody.contains("passcode") ||
                lowerBody.contains("pin") ||
                lowerBody.contains("do not share") ||
                lowerBody.contains("one time") ||
                lowerBody.contains("valid for")

        if (!isLikelyOtpMessage) return null

        var extractedCode: String? = null

        // Try strict pattern first
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val candidate = matcher.group(1)
                if (candidate != null && candidate.length in 4..8) {
                    extractedCode = candidate
                    break
                }
            }
        }

        if (extractedCode == null) return null

        // Extract expiry if any
        val expiryMatcher = EXPIRY_PATTERN.matcher(body)
        var expiresAt: Long? = null
        if (expiryMatcher.find()) {
            val number = expiryMatcher.group(1)?.toLongOrNull() ?: 5
            val unit = expiryMatcher.group(2)?.lowercase() ?: "min"
            val durationMillis = when {
                unit.startsWith("sec") -> number * 1000L
                unit.startsWith("hour") -> number * 3600L * 1000L
                else -> number * 60L * 1000L
            }
            expiresAt = timestamp + durationMillis
        } else {
            // Default 10 minutes from arrival
            expiresAt = timestamp + (10 * 60 * 1000L)
        }

        val serviceName = extractServiceName(sender, body)

        return OtpDetails(
            code = extractedCode,
            serviceName = serviceName,
            expiresAt = expiresAt,
            rawContext = body
        )
    }

    fun categorize(sender: String, body: String, otpDetails: OtpDetails?): SmsCategory {
        val text = "${sender.lowercase()} ${body.lowercase()}"
        return when {
            text.contains("bank") || text.contains("chase") || text.contains("wells fargo") ||
                    text.contains("citi") || text.contains("debit") || text.contains("credit") ||
                    text.contains("card") || text.contains("account") || text.contains("transfer") ||
                    text.contains("usd") || text.contains("rs.") || text.contains("$") -> SmsCategory.BANKING

            otpDetails != null -> SmsCategory.OTP

            text.contains("security") || text.contains("alert") || text.contains("login") ||
                    text.contains("password") || text.contains("attempt") || text.contains("device") -> SmsCategory.SECURITY

            else -> SmsCategory.GENERAL
        }
    }

    private fun extractServiceName(sender: String, body: String): String {
        val cleanSender = sender.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
        val text = "${sender.lowercase()} ${body.lowercase()}"

        val knownServices = listOf(
            "Google", "WhatsApp", "Amazon", "Microsoft", "Apple", "Uber",
            "Netflix", "GitHub", "Twitter", "Telegram", "Instagram", "Facebook",
            "PayPal", "Spotify", "Stripe", "Bank of America", "Chase", "Wells Fargo"
        )

        for (service in knownServices) {
            if (text.contains(service.lowercase())) {
                return service
            }
        }

        return if (cleanSender.isNotEmpty() && cleanSender.length < 20) {
            cleanSender.uppercase()
        } else {
            "Verification Service"
        }
    }
}
