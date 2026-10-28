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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertCondition
import com.example.data.model.StockQuote
import com.example.ui.components.CreatePriceAlertDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceAlertsScreen(
    viewModel: TradingViewModel,
    onNavigateToSearch: () -> Unit
) {
    val alerts by viewModel.priceAlerts.collectAsState()
    val allStocks by viewModel.allStocks.collectAsState()

    var showCreateDialogForStock by remember { mutableStateOf<StockQuote?>(null) }
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("price_alerts_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text("Price Alerts", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Automated Threshold & WhatsApp Dispatch", fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (alerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold.copy(alpha = 0.5f), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Active Price Alerts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Create an alert to get notified when stock crosses a target", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                items(alerts) { alert ->
                    val stock = allStocks.find { it.symbol == alert.symbol }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (alert.condition == AlertCondition.ABOVE) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                                ) {
                                    Icon(
                                        imageVector = if (alert.condition == AlertCondition.ABOVE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (alert.condition == AlertCondition.ABOVE) EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.padding(6.dp).size(18.dp)
                                    )
                                }

                                Column {
                                    Text(alert.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        text = "Target: ₹${String.format(Locale.US, "%.2f", alert.targetPrice)} (${alert.condition.name.lowercase().replaceFirstChar { it.uppercase() }})",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    stock?.let { s ->
                                        Text("Current: ${s.formattedPrice}", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (alert.isActive) CyanAccent.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (alert.isActive) "ACTIVE" else "TRIGGERED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.isActive) CyanAccent else TextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(onClick = { viewModel.deletePriceAlert(alert.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to quickly create alert
        FloatingActionButton(
            onClick = {
                val stock = allStocks.firstOrNull()
                if (stock != null) showCreateDialogForStock = stock
            },
            containerColor = AmberGold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .testTag("add_price_alert_fab")
        ) {
            Icon(Icons.Default.AddAlert, contentDescription = "Create Alert", tint = NavyDeep)
        }
    }

    showCreateDialogForStock?.let { stock ->
        CreatePriceAlertDialog(
            stock = stock,
            onDismiss = { showCreateDialogForStock = null },
            onConfirm = { price, condition ->
                viewModel.createPriceAlert(stock.symbol, price, condition)
                showCreateDialogForStock = null
            }
        )
    }
}
