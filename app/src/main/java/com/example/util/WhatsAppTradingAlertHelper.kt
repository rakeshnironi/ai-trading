package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationLogEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AIRecommendation
import com.example.data.model.NotificationEventType
import com.example.data.model.NotificationStatus
import com.example.data.model.SignalType
import com.example.data.model.WhatsAppNotificationLog
import com.example.data.remote.WhatsAppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

data class TradingAlertResult(
    val success: Boolean,
    val log: WhatsAppNotificationLog?,
    val message: String,
    val isThrottled: Boolean = false,
    val action: SignalType,
    val symbol: String
)

/**
 * WhatsAppTradingAlertHelper
 *
 * Dedicated helper class for formatting, dispatching, and managing automated trading alerts
 * via WhatsApp whenever the ML model triggers a BUY or SELL signal.
 *
 * Integrates with:
 * 1. Meta WhatsApp Cloud API (via WhatsAppService)
 * 2. Android native high-priority heads-up notifications
 * 3. Deep-link direct WhatsApp execution (api.whatsapp.com)
 * 4. Local Room database audit logging
 */
object WhatsAppTradingAlertHelper {

    private const val CHANNEL_ID = "whatsapp_ai_trading_signals"
    private const val CHANNEL_NAME = "WhatsApp Automated AI Trading Signals"
    private const val NOTIFICATION_BASE_ID = 2000

    // Cooldown tracker: symbol + signal -> timestamp of last dispatch to avoid duplicate spam
    private val lastAlertTimestamps = ConcurrentHashMap<String, Long>()
    private const val ALERT_COOLDOWN_MS = 90_000L // 90 seconds per symbol/signal cooldown

    /**
     * Format an executive, high-impact trading alert message for WhatsApp.
     */
    fun formatTradingAlertMessage(
        recommendation: AIRecommendation,
        clientCode: String? = null,
        marketStatusText: String? = null
    ): String {
        val istSdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a 'IST'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        val currentTimeStr = istSdf.format(Date())

        val actionEmoji = when (recommendation.signal) {
            SignalType.BUY -> "🟢 *STRONG BUY (LONG)*"
            SignalType.SELL -> "🔴 *STRONG SELL (PROFIT/SHORT)*"
            SignalType.HOLD -> "🟡 *HOLD / NEUTRAL*"
        }

        val targetDiff = recommendation.targetPrice - recommendation.currentPrice
        val targetPct = if (recommendation.currentPrice > 0) {
            (abs(targetDiff) / recommendation.currentPrice) * 100.0
        } else 0.0

        val slDiff = recommendation.currentPrice - recommendation.stopLoss
        val slPct = if (recommendation.currentPrice > 0) {
            (abs(slDiff) / recommendation.currentPrice) * 100.0
        } else 0.0

        val rrRatio = if (slPct > 0) String.format(Locale.US, "%.1f", targetPct / slPct) else "2.0"
        val marketStatus = marketStatusText ?: MarketScheduleHelper.getMarketStatus().statusLabel

        val factorsFormatted = recommendation.primaryReasons
            .take(4)
            .joinToString("\n") { "• $it" }

        return buildString {
            append("🤖 *AUTONOMOUS TRADING BOT: ML SIGNAL*\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("⚡ *Action:* $actionEmoji\n")
            append("📌 *Symbol:* *${recommendation.symbol}* (NSE / BSE)\n")
            append("💵 *Entry LTP:* ₹${String.format(Locale.US, "%,.2f", recommendation.currentPrice)}\n")
            append("🎯 *Target (2.5× ATR):* ₹${String.format(Locale.US, "%,.2f", recommendation.targetPrice)} (+${String.format(Locale.US, "%.2f", targetPct)}%)\n")
            append("🛑 *Stop Loss (1.5× ATR):* ₹${String.format(Locale.US, "%,.2f", recommendation.stopLoss)} (-${String.format(Locale.US, "%.2f", slPct)}%)\n")
            append("⚖️ *Risk/Reward Ratio:* 1 : $rrRatio\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("🧠 *ML Confidence:* ${recommendation.confidence}% (${recommendation.riskLevel.name} Risk)\n")
            append("📊 *Consensus:* ${recommendation.indicatorConsensus}\n\n")
            append("🔍 *Key Model Rationale:*\n")
            append(factorsFormatted)
            append("\n\n")
            append("🏛️ *Market Status:* $marketStatus\n")
            if (!clientCode.isNullOrBlank()) {
                append("👤 *Account:* $clientCode\n")
            }
            append("⏱️ *Timestamp:* $currentTimeStr\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("⚠️ *Regulatory Notice:*\n")
            append("Algorithmic signal generated via XGBoost + Random Forest ensemble. Educational automated alert.")
        }
    }

    /**
     * Send automated trading alert via WhatsApp service/API whenever ML triggers BUY or SELL.
     */
    suspend fun sendAutomatedSignalAlert(
        context: Context?,
        recommendation: AIRecommendation,
        profile: UserProfileEntity,
        forceSend: Boolean = false,
        db: AppDatabase? = null
    ): TradingAlertResult = withContext(Dispatchers.IO) {
        val signal = recommendation.signal

        // 1. Filter out HOLD signals unless explicitly forced
        if (signal == SignalType.HOLD && !forceSend) {
            return@withContext TradingAlertResult(
                success = false,
                log = null,
                message = "HOLD signal skipped — automated alerts only trigger on BUY or SELL actions.",
                action = signal,
                symbol = recommendation.symbol
            )
        }

        // 2. Check if automated alerts are permitted in profile
        if (!profile.isWhatsappEnabled && !forceSend) {
            return@withContext TradingAlertResult(
                success = false,
                log = null,
                message = "WhatsApp notifications are disabled in user profile settings.",
                action = signal,
                symbol = recommendation.symbol
            )
        }

        // 3. Cooldown check per symbol to avoid spamming the user
        val cooldownKey = "${recommendation.symbol}_${signal.name}"
        val now = System.currentTimeMillis()
        val lastSent = lastAlertTimestamps[cooldownKey] ?: 0L
        if (!forceSend && (now - lastSent) < ALERT_COOLDOWN_MS) {
            val remainingSec = ((ALERT_COOLDOWN_MS - (now - lastSent)) / 1000).coerceAtLeast(1)
            return@withContext TradingAlertResult(
                success = true,
                log = null,
                message = "Alert throttled for ${recommendation.symbol} (${remainingSec}s cooldown remaining).",
                isThrottled = true,
                action = signal,
                symbol = recommendation.symbol
            )
        }

        // 4. Format executive WhatsApp alert
        val messageText = formatTradingAlertMessage(
            recommendation = recommendation,
            clientCode = profile.angelClientId.ifEmpty { "RN9942" }
        )

        val recipientPhone = profile.phone.ifEmpty { "+91 9876543210" }

        // 5. Dispatch via WhatsApp Cloud API / WhatsAppService
        val log = WhatsAppService.sendWhatsAppNotification(
            apiKey = profile.whatsappApiKey,
            phoneNumberId = profile.whatsappPhoneNumberId,
            recipientNumber = recipientPhone,
            eventType = NotificationEventType.AI_SIGNAL,
            messageText = messageText
        )

        // 6. Record in Room database
        db?.notificationLogDao()?.insertLog(
            NotificationLogEntity(
                eventType = log.eventType.name,
                recipientNumber = log.recipientNumber,
                messageText = log.messageText,
                status = log.status.name,
                timestamp = log.timestamp,
                errorDetails = log.errorDetails
            )
        )

        // 7. Update cooldown tracker
        lastAlertTimestamps[cooldownKey] = now

        // 8. Post native Android heads-up notification if context is provided
        context?.let { ctx ->
            triggerHeadsUpNotification(ctx, recommendation, messageText)
        }

        TradingAlertResult(
            success = log.status == NotificationStatus.SENT,
            log = log,
            message = "Automated WhatsApp ${signal.name} alert sent for ${recommendation.symbol} to $recipientPhone",
            isThrottled = false,
            action = signal,
            symbol = recommendation.symbol
        )
    }

    /**
     * Post a native Android heads-up banner notification on the device.
     */
    fun triggerHeadsUpNotification(
        context: Context,
        recommendation: AIRecommendation,
        messageText: String
    ) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // Create notification channel for Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (existing == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Instant WhatsApp & ML automated trading signals"
                        enableVibration(true)
                        setShowBadge(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("selected_stock", recommendation.symbol)
                putExtra("navigate_to", "recommendation")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                recommendation.symbol.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val actionTitle = if (recommendation.signal == SignalType.BUY) "🟢 BUY" else "🔴 SELL"
            val title = "WhatsApp Trading Alert: $actionTitle ${recommendation.symbol} @ ₹${String.format(Locale.US, "%,.2f", recommendation.currentPrice)}"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Target ₹${String.format(Locale.US, "%,.2f", recommendation.targetPrice)} • SL ₹${String.format(Locale.US, "%,.2f", recommendation.stopLoss)} • Confidence ${recommendation.confidence}%")
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notifId = NOTIFICATION_BASE_ID + abs(recommendation.symbol.hashCode() % 1000)
            notificationManager.notify(notifId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Direct launch of WhatsApp with pre-filled message text.
     */
    fun openWhatsAppDirect(context: Context, mobileNumber: String, messageText: String) {
        try {
            val cleanDigits = mobileNumber.filter { it.isDigit() }.takeLast(10)
            val fullPhone = "91$cleanDigits"
            val url = "https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(messageText)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Trading Alert"))
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Reset throttling cache for tests or immediate re-triggering.
     */
    fun clearCooldowns() {
        lastAlertTimestamps.clear()
    }
}
