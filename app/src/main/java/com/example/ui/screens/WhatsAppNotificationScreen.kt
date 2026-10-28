package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationStatus
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppNotificationScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit
) {
    val logs by viewModel.notificationLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("whatsapp_logs_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Column {
                    Text("WhatsApp Notification Gateway", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Official Cloud API Dispatch History", fontSize = 11.sp, color = Color(0xFF25D366))
                }
            }
        }

        // WhatsApp Gateway Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                            Text("Gateway Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF25D366).copy(alpha = 0.15f)) {
                            Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF25D366), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text("Recipient WhatsApp: ${profile?.phone ?: "+91 9876543210"}", fontSize = 12.sp, color = TextSecondary)
                    Text("Dispatches real-time alerts for Paper Trades, ML Signals, Price Thresholds, and Daily Summaries.", fontSize = 11.sp, color = TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Logs Header
        item {
            Text(
                text = "Dispatched Alert Records (${logs.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No WhatsApp Logs Recorded Yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Execute a trade or trigger an AI signal to dispatch a message", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = log.eventType.name.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (log.status == NotificationStatus.SENT) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (log.status == NotificationStatus.SENT) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (log.status == NotificationStatus.SENT) EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = log.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.status == NotificationStatus.SENT) EmeraldGreen else CrimsonRed
                                    )
                                }
                            }
                        }

                        Text(
                            text = sdf.format(Date(log.timestamp)),
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        // Message Payload Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = log.messageText,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }

                        log.errorDetails?.let { err ->
                            Text("Details: $err", fontSize = 11.sp, color = CrimsonRed)
                        }
                    }
                }
            }
        }
    }
}
