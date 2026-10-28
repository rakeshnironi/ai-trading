package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockQuote
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
import com.example.data.model.TradingMode
import com.example.ui.components.ModeSelectionBottomSheet
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val watchlists by viewModel.customWatchlists.collectAsState()
    val selectedTabId by viewModel.selectedWatchlistTabId.collectAsState()
    val selectedSegment by viewModel.selectedSegmentFilter.collectAsState()
    val allStocks by viewModel.allStocks.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateWatchlistDialog by remember { mutableStateOf(false) }
    var newWatchlistName by remember { mutableStateOf("") }
    var showAddStockDialog by remember { mutableStateOf(false) }
    var showModeSelectionSheet by remember { mutableStateOf(false) }

    val currentTab = watchlists.find { it.id == selectedTabId } ?: watchlists.firstOrNull()
    val selectedTabIndex = watchlists.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)

    // Segment & Search filtering
    val currentTabSymbols = currentTab?.symbols ?: emptyList()
    val filteredStocks = allStocks.filter { stock ->
        val inWatchlist = currentTabSymbols.contains(stock.symbol)
        val matchesSearch = searchQuery.isBlank() ||
                stock.symbol.contains(searchQuery, ignoreCase = true) ||
                stock.companyName.contains(searchQuery, ignoreCase = true)
        val matchesSegment = when (selectedSegment) {
            "EQUITY" -> stock.sector != "Index"
            "F&O" -> stock.currentPrice > 500 || stock.symbol in listOf("RELIANCE", "NIFTY", "BANKNIFTY", "TATAMOTORS")
            "COMMODITY" -> stock.sector.contains("Metals", ignoreCase = true) || stock.sector.contains("Energy", ignoreCase = true)
            "CURRENCY" -> false
            else -> true
        }
        inWatchlist && matchesSearch && matchesSegment
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("watchlist_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Search & Angel One Watchlist Tabs
            Surface(
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Top Row: Title + Search & Add Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isLive = tradingMode == TradingMode.LIVE
                        val modeColor = if (isLive) LiveMarketGreen else PaperTradingBlue
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Watchlist",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showModeSelectionSheet = true }
                                        .testTag("watchlist_mode_badge"),
                                    color = modeColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).background(modeColor, CircleShape))
                                        Text(
                                            text = if (isLive) "LIVE" else "PAPER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = modeColor
                                        )
                                        Icon(Icons.Default.Sync, contentDescription = "Switch", tint = modeColor, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                            Text(
                                text = if (isLive) "Live SmartAPI prices • ${currentTab?.name ?: "All"} (${filteredStocks.size})" else "Simulated prices • ${currentTab?.name ?: "All"} (${filteredStocks.size})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onNavigateToSearch,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NavyCard, CircleShape)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search Global", tint = SleekLavender, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { showCreateWatchlistDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NavyCard, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New Watchlist", tint = AmberGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // In-page Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search in ${currentTab?.name ?: "Watchlist"}...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard,
                            focusedBorderColor = SleekLavender,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Watchlist Tabs (Scrollable)
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = SleekLavender,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = SleekLavender,
                                    height = 3.dp
                                )
                            }
                        },
                        divider = {}
                    ) {
                        watchlists.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.selectWatchlistTab(tab.id) },
                                text = {
                                    Text(
                                        text = tab.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == index) SleekLavender else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    // Segment Filters Row (ALL, EQUITY, F&O, COMMODITY, CURRENCY)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("ALL", "EQUITY", "F&O", "COMMODITY", "CURRENCY")) { seg ->
                            FilterChip(
                                selected = selectedSegment == seg,
                                onClick = { viewModel.setSegmentFilter(seg) },
                                label = { Text(seg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekLavender.copy(alpha = 0.2f),
                                    selectedLabelColor = SleekLavender,
                                    containerColor = NavyCard,
                                    labelColor = TextSecondary
                                ),
                                border = BorderStroke(1.dp, if (selectedSegment == seg) SleekLavender else NavyBorder)
                            )
                        }
                    }
                }
            }

            // Watchlist Content
            if (filteredStocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold.copy(alpha = 0.4f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No stocks in this watchlist", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add stocks to track real-time LTP & price action", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddStockDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Stocks Now", color = NavyDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStocks, key = { it.symbol }) { stock ->
                        AngelOneStockWatchTile(
                            stock = stock,
                            onClick = { onNavigateToStockDetail(stock.symbol) },
                            onDelete = { currentTab?.id?.let { viewModel.removeStockFromWatchlist(stock.symbol, it) } }
                        )
                    }
                }
            }
        }

        // Add Stock FAB
        FloatingActionButton(
            onClick = { showAddStockDialog = true },
            containerColor = SleekLavender,
            contentColor = NavyDeep,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 85.dp, end = 20.dp)
                .testTag("fab_add_stock")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Stock")
        }

        // Dialog: Create New Watchlist Tab
        if (showCreateWatchlistDialog) {
            AlertDialog(
                onDismissRequest = { showCreateWatchlistDialog = false },
                containerColor = NavyCardElevated,
                title = { Text("Create New Watchlist", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Column {
                        Text("Give your watchlist a name (e.g. Midcaps, FMCG, Intraday)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newWatchlistName,
                            onValueChange = { newWatchlistName = it },
                            placeholder = { Text("Watchlist Name") },
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
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newWatchlistName.isNotBlank()) {
                                viewModel.addCustomWatchlist(newWatchlistName)
                                newWatchlistName = ""
                                showCreateWatchlistDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekLavender)
                    ) {
                        Text("Create", color = NavyDeep, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showCreateWatchlistDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Dialog: Add Stocks into Current Watchlist
        if (showAddStockDialog) {
            val availableToAdd = allStocks.filter { !currentTabSymbols.contains(it.symbol) }
            AlertDialog(
                onDismissRequest = { showAddStockDialog = false },
                containerColor = NavyCardElevated,
                title = { Text("Add Stocks to ${currentTab?.name ?: "Watchlist"}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(availableToAdd) { stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addStockToWatchlist(stock.symbol, currentTab?.id)
                                        showAddStockDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stock.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(stock.companyName, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                }
                                Text(stock.formattedPrice, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                            }
                        }
                    }
                },
                confirmButton = {
                    OutlinedButton(onClick = { showAddStockDialog = false }) {
                        Text("Done", color = TextSecondary)
                    }
                }
            )
        }

        if (showModeSelectionSheet) {
            ModeSelectionBottomSheet(
                currentMode = tradingMode,
                onDismissRequest = { showModeSelectionSheet = false },
                onModeConfirmed = { newMode ->
                    viewModel.switchTradingMode(newMode)
                }
            )
        }
    }
}

@Composable
fun AngelOneStockWatchTile(
    stock: StockQuote,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isPositive = stock.isPositive
    val isTopGainer = stock.percentChange >= 2.5
    val isTopLoser = stock.percentChange <= -2.0
    val isNear52WHigh = (stock.high52Week - stock.currentPrice) / stock.high52Week < 0.05
    val isNear52WLow = (stock.currentPrice - stock.low52Week) / stock.low52Week < 0.05

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("watchlist_item_${stock.symbol}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Symbol, Company & Angel One Smart Tags
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.symbol,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stock.exchange.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stock.companyName,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Angel One Market Tags: Top Gainers, Losers, 52W High/Low
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isTopGainer) {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Top Gainer", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    if (isTopLoser) {
                        Surface(
                            color = CrimsonRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Top Loser", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CrimsonRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    if (isNear52WHigh) {
                        Surface(
                            color = AmberGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("52W High", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberGold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    if (isNear52WLow) {
                        Surface(
                            color = CyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("52W Low", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            // Right: Price, Change %, 52W Range & Delete Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stock.formattedPrice,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) EmeraldGreen else CrimsonRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${stock.formattedPercent} (${stock.formattedChange})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) EmeraldGreen else CrimsonRed
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
