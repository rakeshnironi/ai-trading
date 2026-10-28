package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalType
import com.example.ui.components.SignalBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SleekDarkPurple
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaperTradingScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: (String) -> Unit
) {
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val transactions by viewModel.paperTransactions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("paper_trading_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Paper Trading Engine", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Zero-Risk Simulation (₹1,00,000 Capital)", fontSize = 12.sp, color = TextSecondary)
                }

                IconButton(
                    onClick = { viewModel.resetVirtualWallet() },
                    modifier = Modifier
                        .background(NavyCard, CircleShape)
                        .border(1.dp, NavyBorder, CircleShape)
                        .size(38.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Wallet", tint = SleekLavender)
                }
            }
        }

        // Virtual Wallet Capital Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL VIRTUAL VALUATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = TextSecondary)
                        Surface(shape = RoundedCornerShape(20.dp), color = EmeraldGreen.copy(alpha = 0.12f), border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))) {
                            Text("Angel One Live", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }

                    Text(
                        text = portfolioSummary?.formattedTotal ?: "₹1,00,000.00",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyDeep, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Cash", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(portfolioSummary?.formattedCash ?: "₹1,00,000.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Invested In Equities", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(portfolioSummary?.formattedInvested ?: "₹0.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Transaction History Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(18.dp))
                    Text("Trade Execution Ledger", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text("${transactions.size} Trades", fontSize = 12.sp, color = TextMuted)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Paper Trades Executed Yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Explore stocks and execute your first paper trade!", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SignalBadge(signal = tx.action)
                                Column {
                                    Text(tx.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(sdf.format(Date(tx.timestamp)), fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format(Locale.US, "%,.2f", tx.totalValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${tx.quantity} shares @ ₹${String.format("%.2f", tx.executionPrice)}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        if (tx.action == SignalType.SELL) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Realized Profit/Loss:", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = (if (tx.realizedPnl >= 0) "+₹" else "-₹") + String.format(Locale.US, "%,.2f", Math.abs(tx.realizedPnl)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.realizedPnl >= 0) EmeraldGreen else CrimsonRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
