package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AngelOneOrder
import com.example.data.model.BasketOrder
import com.example.data.model.GttRule
import com.example.data.model.OrderProductType
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.SignalType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: (String) -> Unit
) {
    val orders by viewModel.angelOneOrders.collectAsState()
    val gttRules by viewModel.gttRules.collectAsState()
    val baskets by viewModel.basketOrders.collectAsState()
    val holdings by viewModel.portfolioHoldings.collectAsState()
    val openPositions by viewModel.openPositions.collectAsState()
    val allStocks by viewModel.allStocks.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Open ( ${orders.count { it.status == OrderStatus.OPEN }} )", "Positions", "Executed", "GTT", "Baskets")

    var showCreateOrderDialog by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("orders_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Angel One Header
            Surface(
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Orders",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Open Orders • Intraday Positions • GTT • Baskets",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { showCreateOrderDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("place_order_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Angel One Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = SleekLavender,
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = SleekLavender,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == index) SleekLavender else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> OpenOrdersTab(
                    orders = orders.filter { it.status == OrderStatus.OPEN },
                    onCancelOrder = { viewModel.cancelAngelOneOrder(it) },
                    onNavigateToStock = onNavigateToStockDetail,
                    onOpenNewOrder = { showCreateOrderDialog = true }
                )
                1 -> PositionsTab(
                    holdings = holdings,
                    positions = openPositions,
                    onNavigateToStock = onNavigateToStockDetail
                )
                2 -> ExecutedOrdersTab(
                    orders = orders.filter { it.status != OrderStatus.OPEN },
                    onNavigateToStock = onNavigateToStockDetail,
                    sdf = sdf
                )
                3 -> GttRulesTab(
                    gttRules = gttRules,
                    onCancelRule = { viewModel.cancelGttRule(it) },
                    onNavigateToStock = onNavigateToStockDetail
                )
                4 -> BasketOrdersTab(
                    baskets = baskets,
                    onExecuteBasket = { viewModel.executeBasketOrder(it) }
                )
            }
        }

        // Fast Order Placement Floating Button
        FloatingActionButton(
            onClick = { showCreateOrderDialog = true },
            containerColor = SleekLavender,
            contentColor = NavyDeep,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 85.dp, end = 20.dp)
                .testTag("fab_new_order")
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = "Place Order")
        }

        // New Order Modal Dialog
        if (showCreateOrderDialog) {
            AngelOneOrderDialog(
                stocks = allStocks.map { it.symbol },
                onDismiss = { showCreateOrderDialog = false },
                onPlaceOrder = { symbol, action, type, product, qty, price, trigger ->
                    viewModel.placeAngelOneOrder(symbol, action, type, product, qty, price, trigger)
                    showCreateOrderDialog = false
                }
            )
        }
    }
}

@Composable
private fun OpenOrdersTab(
    orders: List<AngelOneOrder>,
    onCancelOrder: (String) -> Unit,
    onNavigateToStock: (String) -> Unit,
    onOpenNewOrder: () -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Open Orders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Pending limit and stop-loss orders will appear here", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenNewOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Place an Order", color = NavyDeep, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStock(order.symbol) },
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (order.action == SignalType.BUY) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = order.action.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.action == SignalType.BUY) EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("• ${order.productType.code}", fontSize = 11.sp, color = TextSecondary)
                            }

                            Surface(
                                color = AmberGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "PENDING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Quantity", fontSize = 11.sp, color = TextSecondary)
                                Text("${order.quantity} shares", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("Order Type", fontSize = 11.sp, color = TextSecondary)
                                Text(order.orderType.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Price", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${String.format(Locale.US, "%,.2f", order.price)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        if (order.triggerPrice != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Trigger Price: ₹${String.format(Locale.US, "%,.2f", order.triggerPrice)}", fontSize = 11.sp, color = AmberGold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onCancelOrder(order.orderId) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Cancel Order", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionsTab(
    holdings: List<com.example.data.model.HoldingItem>,
    positions: List<com.example.data.local.TradePositionEntity>,
    onNavigateToStock: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Live P&L Summary Bar
            val totalPnl = holdings.sumOf { it.pnl } + positions.sumOf { it.unrealizedPnL }
            val isPositive = totalPnl >= 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Open Positions P&L", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${if (isPositive) "+" else ""}₹${String.format(Locale.US, "%,.2f", totalPnl)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPositive) EmeraldGreen else CrimsonRed
                        )
                    }
                    Surface(
                        color = (if (isPositive) EmeraldGreen else CrimsonRed).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${holdings.size + positions.size} Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) EmeraldGreen else CrimsonRed
                            )
                        }
                    }
                }
            }
        }

        if (holdings.isEmpty() && positions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active intraday or F&O positions currently open", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }

        items(holdings) { holding ->
            val pnl = holding.pnl
            val isGreen = pnl >= 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStock(holding.symbol) },
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = CyanAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("CNC / EQ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(holding.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Qty: ${holding.quantity}", fontSize = 12.sp, color = TextSecondary)
                        }

                        Text(
                            text = "${if (isGreen) "+" else ""}₹${String.format(Locale.US, "%,.2f", pnl)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGreen) EmeraldGreen else CrimsonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Avg: ₹${String.format(Locale.US, "%.2f", holding.avgBuyPrice)}", fontSize = 12.sp, color = TextSecondary)
                        Text("LTP: ₹${String.format(Locale.US, "%.2f", holding.currentLtp)}", fontSize = 12.sp, color = TextPrimary)
                        Text(
                            text = "${if (holding.pnlPercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f%%", holding.pnlPercentage)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGreen) EmeraldGreen else CrimsonRed
                        )
                    }
                }
            }
        }

        items(positions) { pos ->
            val pnl = pos.unrealizedPnL
            val isGreen = pnl >= 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStock(pos.symbol) },
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SleekLavender.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(pos.productType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekLavender, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(pos.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Qty: ${pos.quantity}", fontSize = 12.sp, color = TextSecondary)
                        }

                        Text(
                            text = "${if (isGreen) "+" else ""}₹${String.format(Locale.US, "%,.2f", pnl)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGreen) EmeraldGreen else CrimsonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Entry: ₹${String.format(Locale.US, "%.2f", pos.entryPrice)}", fontSize = 12.sp, color = TextSecondary)
                        Text("LTP: ₹${String.format(Locale.US, "%.2f", pos.currentLtp)}", fontSize = 12.sp, color = TextPrimary)
                        Text(
                            text = "${if (pos.unrealizedPnLPercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f%%", pos.unrealizedPnLPercent)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGreen) EmeraldGreen else CrimsonRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutedOrdersTab(
    orders: List<AngelOneOrder>,
    onNavigateToStock: (String) -> Unit,
    sdf: SimpleDateFormat
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No executed orders yet", fontSize = 14.sp, color = TextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStock(order.symbol) },
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (order.action == SignalType.BUY) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = order.action.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.action == SignalType.BUY) EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Surface(
                                color = if (order.status == OrderStatus.EXECUTED) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = order.status.displayName.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status == OrderStatus.EXECUTED) EmeraldGreen else CrimsonRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${order.quantity} shares @ ₹${String.format(Locale.US, "%,.2f", order.executedPrice ?: order.price)}", fontSize = 13.sp, color = TextPrimary)
                            Text(sdf.format(Date(order.timestamp)), fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GttRulesTab(
    gttRules: List<GttRule>,
    onCancelRule: (String) -> Unit,
    onNavigateToStock: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Good Till Triggered (GTT)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    Text("Triggers stay active for 1 year until your condition is met on Angel One.", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        items(gttRules) { rule ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStock(rule.symbol) },
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SleekLavender.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("GTT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekLavender, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(rule.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(rule.action.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (rule.action == SignalType.BUY) EmeraldGreen else CrimsonRed)
                        }

                        IconButton(
                            onClick = { onCancelRule(rule.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete GTT", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Trigger Condition", fontSize = 11.sp, color = TextSecondary)
                            Text(rule.triggerCondition, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                        Column {
                            Text("Limit Price", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${String.format(Locale.US, "%,.2f", rule.limitPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Quantity", fontSize = 11.sp, color = TextSecondary)
                            Text("${rule.quantity} shares", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BasketOrdersTab(
    baskets: List<BasketOrder>,
    onExecuteBasket: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Basket Orders", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    Text("Bundle multiple equity and F&O legs together for single-click execution.", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        items(baskets) { basket ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(basket.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(basket.description, fontSize = 11.sp, color = TextSecondary)
                        }

                        Button(
                            onClick = { onExecuteBasket(basket.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (basket.isExecuted) EmeraldGreen.copy(alpha = 0.2f) else SleekLavender
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (basket.isExecuted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (basket.isExecuted) EmeraldGreen else NavyDeep,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (basket.isExecuted) "Executed" else "Execute All",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (basket.isExecuted) EmeraldGreen else NavyDeep
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("${basket.items.size} Legs in Basket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                    Spacer(modifier = Modifier.height(6.dp))

                    basket.items.forEach { leg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${leg.action.name} ${leg.quantity}x ${leg.symbol}", fontSize = 12.sp, color = TextPrimary)
                            Text("₹${String.format(Locale.US, "%,.2f", leg.estimatedAmount)}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Est. Margin", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${String.format(Locale.US, "%,.2f", basket.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AngelOneOrderDialog(
    stocks: List<String>,
    onDismiss: () -> Unit,
    onPlaceOrder: (String, SignalType, OrderType, OrderProductType, Int, Double, Double?) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(stocks.firstOrNull() ?: "RELIANCE") }
    var selectedAction by remember { mutableStateOf(SignalType.BUY) }
    var selectedOrderType by remember { mutableStateOf(OrderType.MARKET) }
    var selectedProductType by remember { mutableStateOf(OrderProductType.DELIVERY) }
    var quantityText by remember { mutableStateOf("10") }
    var priceText by remember { mutableStateOf("2920.0") }
    var triggerText by remember { mutableStateOf("2900.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCardElevated,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Place Angel One Order", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // BUY / SELL Segment Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedAction = SignalType.BUY },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedAction == SignalType.BUY) EmeraldGreen else NavySurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BUY", fontWeight = FontWeight.Bold, color = if (selectedAction == SignalType.BUY) NavyDeep else TextSecondary)
                    }
                    Button(
                        onClick = { selectedAction = SignalType.SELL },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedAction == SignalType.SELL) CrimsonRed else NavySurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SELL", fontWeight = FontWeight.Bold, color = if (selectedAction == SignalType.SELL) Color.White else TextSecondary)
                    }
                }

                // Symbol selection chips
                Text("Select Instrument", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    stocks.take(4).forEach { sym ->
                        FilterChip(
                            selected = selectedSymbol == sym,
                            onClick = { selectedSymbol = sym },
                            label = { Text(sym, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekLavender,
                                selectedLabelColor = NavyDeep
                            )
                        )
                    }
                }

                // Order Type: Market, Limit, SL, GTT
                Text("Order Type", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(OrderType.MARKET, OrderType.LIMIT, OrderType.STOP_LOSS, OrderType.GTT).forEach { type ->
                        FilterChip(
                            selected = selectedOrderType == type,
                            onClick = { selectedOrderType = type },
                            label = { Text(type.displayName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekLavender,
                                selectedLabelColor = NavyDeep
                            )
                        )
                    }
                }

                // Product Type: Delivery vs Intraday
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedProductType == OrderProductType.DELIVERY,
                        onClick = { selectedProductType = OrderProductType.DELIVERY },
                        label = { Text("Delivery (CNC)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedProductType == OrderProductType.INTRADAY,
                        onClick = { selectedProductType = OrderProductType.INTRADAY },
                        label = { Text("Intraday (MIS)", fontSize = 11.sp) }
                    )
                }

                // Quantity input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity (Shares)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekLavender,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Limit Price if not Market
                if (selectedOrderType != OrderType.MARKET) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Limit Price (₹)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekLavender,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Trigger Price if Stop Loss or GTT
                if (selectedOrderType == OrderType.STOP_LOSS || selectedOrderType == OrderType.GTT) {
                    OutlinedTextField(
                        value = triggerText,
                        onValueChange = { triggerText = it },
                        label = { Text("Trigger Price (₹)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekLavender,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    val prc = priceText.toDoubleOrNull() ?: 0.0
                    val trg = triggerText.toDoubleOrNull()
                    onPlaceOrder(selectedSymbol, selectedAction, selectedOrderType, selectedProductType, qty, prc, trg)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedAction == SignalType.BUY) EmeraldGreen else CrimsonRed
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm ${selectedAction.name}", color = if (selectedAction == SignalType.BUY) NavyDeep else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
