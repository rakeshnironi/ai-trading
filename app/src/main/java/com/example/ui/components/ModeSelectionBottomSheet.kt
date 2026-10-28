package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingMode
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PaperTradingBlue
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Groww + Angel One inspired Account Mode Selection Bottom Sheet.
 * Allows switching between Live Market (Green theme) and Paper Trading (Blue theme).
 * Requires confirmation before global state change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionBottomSheet(
    currentMode: TradingMode,
    onDismissRequest: () -> Unit,
    onModeConfirmed: (TradingMode) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var pendingSwitchMode by remember { mutableStateOf<TradingMode?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = NavySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NavyBorder)
            )
        },
        modifier = Modifier.testTag("mode_selection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekLavender,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trading Mode Selection",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Switch between Real Execution & Risk-free Simulation",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("close_mode_sheet")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Active Mode Indicator Pill
            Surface(
                color = if (currentMode == TradingMode.LIVE) LiveMarketGreen.copy(alpha = 0.12f) else PaperTradingBlue.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (currentMode == TradingMode.LIVE) LiveMarketGreen.copy(alpha = 0.4f) else PaperTradingBlue.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (currentMode == TradingMode.LIVE) LiveMarketGreen else PaperTradingBlue,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Environment: ${if (currentMode == TradingMode.LIVE) "Live Market (Real Money)" else "Paper Trading (Virtual ₹1,00,000)"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentMode == TradingMode.LIVE) LiveMarketGreen else PaperTradingBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: Live Market Card (Green Theme #00C853)
            ModeSelectCard(
                mode = TradingMode.LIVE,
                title = "Live Market",
                subtitle = "Real trading mode",
                description = "Connects directly to Angel One SmartAPI WebSocket for actual market execution, live order book, Demat holdings, and real wallet funds.",
                themeColor = LiveMarketGreen,
                badgeText = "LIVE",
                statusSubtitle = "Connected to Market",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                isSelected = currentMode == TradingMode.LIVE,
                features = listOf(
                    "Real SmartAPI market prices",
                    "Real order execution & broker RMS",
                    "Demat portfolio holdings",
                    "Real wallet cash balance",
                    "Official Angel One exchange feed"
                ),
                onClick = {
                    if (currentMode != TradingMode.LIVE) {
                        pendingSwitchMode = TradingMode.LIVE
                        showConfirmationDialog = true
                    }
                },
                modifier = Modifier.testTag("mode_card_live")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Paper Trading Card (Blue Theme #2979FF)
            ModeSelectCard(
                mode = TradingMode.PAPER,
                title = "Paper Trading",
                subtitle = "Practice mode",
                description = "Risk-free practice environment with a virtual ₹1,00,000 balance. Simulated prices, orders, and portfolio without risking real capital.",
                themeColor = PaperTradingBlue,
                badgeText = "PAPER",
                statusSubtitle = "Simulation Mode",
                icon = Icons.Default.School,
                isSelected = currentMode == TradingMode.PAPER,
                features = listOf(
                    "Simulated price fluctuation",
                    "Virtual order execution & history",
                    "Virtual practice portfolio",
                    "Virtual ₹1,00,000 initial balance",
                    "Safe strategy & indicator testing"
                ),
                onClick = {
                    if (currentMode != TradingMode.PAPER) {
                        pendingSwitchMode = TradingMode.PAPER
                        showConfirmationDialog = true
                    }
                },
                modifier = Modifier.testTag("mode_card_paper")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // System-Wide Switching Comparison Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = SleekLavender,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System-Wide Switching Behavior",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    ModeComparisonRow(module = "Watchlist", live = "Live SmartAPI prices", paper = "Simulated prices")
                    ModeComparisonRow(module = "Charts", live = "Real-time candles", paper = "Historical + simulated candles")
                    ModeComparisonRow(module = "Buy / Sell", live = "Real orders", paper = "Virtual orders")
                    ModeComparisonRow(module = "Portfolio", live = "Real Demat holdings", paper = "Virtual portfolio")
                    ModeComparisonRow(module = "Orders", live = "Actual order book", paper = "Paper order history")
                    ModeComparisonRow(module = "P&L", live = "Real profit / loss", paper = "Virtual profit / loss")
                    ModeComparisonRow(module = "Wallet", live = "Real balance", paper = "Virtual ₹1,00,000 balance")
                }
            }
        }
    }

    // Confirmation Dialog before switching
    if (showConfirmationDialog && pendingSwitchMode != null) {
        val target = pendingSwitchMode!!
        val isTargetLive = target == TradingMode.LIVE
        val targetColor = if (isTargetLive) LiveMarketGreen else PaperTradingBlue

        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                pendingSwitchMode = null
            },
            containerColor = NavyCardElevated,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(targetColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTargetLive) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.School,
                        contentDescription = null,
                        tint = targetColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Switch Trading Mode?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Your entire trading environment will change.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Open positions, orders, and portfolio shown will depend on the selected mode.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = targetColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, targetColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = targetColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTargetLive)
                                    "Switching to Live Market: Real order execution will be enabled with your broker margin."
                                else
                                    "Switching to Paper Trading: Risk-free simulation with ₹1,00,000 virtual balance.",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chosen = target
                        showConfirmationDialog = false
                        pendingSwitchMode = null
                        onModeConfirmed(chosen)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = targetColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_mode_switch_button")
                ) {
                    Text(
                        text = "Switch to ${if (isTargetLive) "Live Market" else "Paper Trading"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmationDialog = false
                        pendingSwitchMode = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.testTag("cancel_mode_switch_button")
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 13.sp)
                }
            }
        )
    }
}

/**
 * Selectable card for Live Market or Paper Trading.
 */
@Composable
private fun ModeSelectCard(
    mode: TradingMode,
    title: String,
    subtitle: String,
    description: String,
    themeColor: Color,
    badgeText: String,
    statusSubtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    features: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColor.copy(alpha = 0.08f) else NavyCard
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) themeColor else NavyBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title + Badge + Check Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(themeColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Badge
                            Surface(
                                color = themeColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColor
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active Mode",
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle status & feature highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(themeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusSubtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Text(
                    text = if (isSelected) "Active Now" else "Tap to Select",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) themeColor else TextSecondary
                )
            }
        }
    }
}

/**
 * Compact row for the system-wide switching comparison table.
 */
@Composable
private fun ModeComparisonRow(
    module: String,
    live: String,
    paper: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = module,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = live,
            fontSize = 11.sp,
            color = LiveMarketGreen,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = paper,
            fontSize = 11.sp,
            color = PaperTradingBlue,
            modifier = Modifier.weight(1.3f)
        )
    }
}
