package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketMoverType
import com.example.data.model.StockQuote
import com.example.ui.components.StockCard
import com.example.ui.theme.AiCardBg
import com.example.ui.theme.AiCardBorder
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
import java.util.Locale

@Composable
fun MarketMoversScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToPrediction: (String) -> Unit
) {
    val selectedMoverType by viewModel.selectedMoverType.collectAsState()
    val topGainers by viewModel.topGainers.collectAsState()
    val topLosers by viewModel.topLosers.collectAsState()
    val volumeMovers by viewModel.volumeMovers.collectAsState()
    val high52WMovers by viewModel.high52WMovers.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()

    val moverTabs = listOf(
        MarketMoverType.TOP_GAINERS,
        MarketMoverType.TOP_LOSERS,
        MarketMoverType.VOLUME_SHOCKERS,
        MarketMoverType.HIGH_52W
    )

    val currentStocks = when (selectedMoverType) {
        MarketMoverType.TOP_GAINERS -> topGainers
        MarketMoverType.TOP_LOSERS -> topLosers
        MarketMoverType.VOLUME_SHOCKERS -> volumeMovers
        MarketMoverType.HIGH_52W -> high52WMovers
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("market_movers_screen")
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("movers_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Market Movers & Gainers",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-Time Angel One SmartAPI Scanner",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Tab Row
        ScrollableTabRow(
            selectedTabIndex = moverTabs.indexOf(selectedMoverType).coerceAtLeast(0),
            containerColor = NavySurface,
            contentColor = CyanAccent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val index = moverTabs.indexOf(selectedMoverType).coerceAtLeast(0)
                if (index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = when (selectedMoverType) {
                            MarketMoverType.TOP_GAINERS -> EmeraldGreen
                            MarketMoverType.TOP_LOSERS -> CrimsonRed
                            MarketMoverType.VOLUME_SHOCKERS -> CyanAccent
                            MarketMoverType.HIGH_52W -> AmberGold
                        },
                        height = 3.dp
                    )
                }
            }
        ) {
            moverTabs.forEach { tab ->
                val isSelected = selectedMoverType == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setMoverType(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (tab) {
                                    MarketMoverType.TOP_GAINERS -> Icons.AutoMirrored.Filled.TrendingUp
                                    MarketMoverType.TOP_LOSERS -> Icons.AutoMirrored.Filled.TrendingDown
                                    MarketMoverType.VOLUME_SHOCKERS -> Icons.Default.LocalFireDepartment
                                    MarketMoverType.HIGH_52W -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = if (isSelected) {
                                    when (tab) {
                                        MarketMoverType.TOP_GAINERS -> EmeraldGreen
                                        MarketMoverType.TOP_LOSERS -> CrimsonRed
                                        MarketMoverType.VOLUME_SHOCKERS -> CyanAccent
                                        MarketMoverType.HIGH_52W -> AmberGold
                                    }
                                } else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("mover_tab_${tab.name}")
                )
            }
        }

        // 3. Info Banner for selected mover
        Surface(
            color = NavyCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedMoverType.subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${currentStocks.size} Instruments",
                    color = CyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Movers List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentStocks, key = { it.symbol }) { stock ->
                val isInWatchlist = watchlist.any { it.symbol == stock.symbol }
                DetailedMoverCard(
                    stock = stock,
                    moverType = selectedMoverType,
                    isInWatchlist = isInWatchlist,
                    onStockClick = { onNavigateToStockDetail(stock.symbol) },
                    onChartClick = { onNavigateToChart(stock.symbol) },
                    onAiClick = { onNavigateToPrediction(stock.symbol) },
                    onToggleWatchlist = { viewModel.toggleWatchlist(stock) }
                )
            }
        }
    }
}

@Composable
fun DetailedMoverCard(
    stock: StockQuote,
    moverType: MarketMoverType,
    isInWatchlist: Boolean,
    onStockClick: () -> Unit,
    onChartClick: () -> Unit,
    onAiClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable { onStockClick() }
            .testTag("mover_card_${stock.symbol}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NavyDeep,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = stock.exchange.name,
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = stock.companyName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Price & Percentage Pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stock.formattedPrice,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = if (stock.isPositive) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stock.formattedPercent,
                            color = if (stock.isPositive) EmeraldGreen else CrimsonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row (Volume, High/Low Range, Sector)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = NavyDeep,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Vol: ${String.format(Locale.US, "%,.1f", stock.volume / 1000000.0)}M",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = NavyDeep,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stock.sector,
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onAiClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Forecast", tint = SleekLavender, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onChartClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.CandlestickChart, contentDescription = "Chart", tint = CyanAccent, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (isInWatchlist) AmberGold else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
