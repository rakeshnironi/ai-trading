package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlin.random.Random

object OtpNotificationHelper {

    private const val CHANNEL_ID = "angel_one_otp_channel"
    private const val CHANNEL_NAME = "Angel One OTP & Security Alerts"
    private const val NOTIFICATION_ID = 1001

    /**
     * Generate a realistic 6-digit numeric OTP code
     */
    fun generateOtp(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    /**
     * Post an authentic high-priority system notification on the user's phone/device
     * so it pops down in the Android notification shade with sound/heads-up banner.
     */
    fun sendOtpNotification(context: Context, mobileNumber: String, otpCode: String) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // Create Notification Channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (existingChannel == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Instant SMS OTP and 2FA login verification alerts for Angel One"
                        enableVibration(true)
                        setShowBadge(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }

            // Intent to open or return to the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("auto_fill_otp", otpCode)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val cleanNumber = if (mobileNumber.length >= 4) "••••" + mobileNumber.takeLast(4) else mobileNumber

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Angel One: OTP is $otpCode")
                .setContentText("$otpCode is your secret verification code for +91 $cleanNumber. Valid for 10 mins.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("<#> $otpCode is your Angel One verification OTP for phone +91 $cleanNumber. Valid for 10 minutes. Please do not share this OTP with anyone, including Angel One employees. \n\n[Security Identifier: VK-ANGELONE]")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // In case notification permissions are restricted, fallback gracefully
            e.printStackTrace()
        }
    }

    /**
     * Copy OTP to device clipboard and show toast
     */
    fun copyToClipboard(context: Context, text: String, message: String = "OTP $text copied to clipboard") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Angel One OTP", text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Dispatch OTP via WhatsApp to user's phone number
     */
    fun sendOtpViaWhatsApp(context: Context, mobileNumber: String, otpCode: String) {
        try {
            val cleanDigits = mobileNumber.filter { it.isDigit() }.takeLast(10)
            val fullPhone = "91$cleanDigits"
            val text = "Your Angel One login verification OTP is *$otpCode*. Valid for 10 minutes. Do not share with anyone."
            val url = "https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(text)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, fallback to generic share
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Your Angel One OTP is $otpCode")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Angel One OTP"))
            } catch (ex: Exception) {
                Toast.makeText(context, "OTP is $otpCode", Toast.LENGTH_LONG).show()
            }
        }
    }
}
