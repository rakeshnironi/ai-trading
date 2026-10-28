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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exchange
import com.example.ui.components.StockCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun StockSearchScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val exchangeFilter by viewModel.selectedExchangeFilter.collectAsState()
    val filteredStocks by viewModel.filteredStocks.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("stock_search_screen")
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by Symbol, Company or Sector", fontSize = 13.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_field")
            )
        }

        // Exchange Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = exchangeFilter == null,
                onClick = { viewModel.setExchangeFilter(null) },
                label = { Text("ALL", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = NavyDeep,
                    containerColor = NavyCard,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = exchangeFilter == Exchange.NSE,
                onClick = { viewModel.setExchangeFilter(Exchange.NSE) },
                label = { Text("NSE Equities", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = NavyDeep,
                    containerColor = NavyCard,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = exchangeFilter == Exchange.BSE,
                onClick = { viewModel.setExchangeFilter(Exchange.BSE) },
                label = { Text("BSE", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = NavyDeep,
                    containerColor = NavyCard,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results count
        Text(
            text = "Angel One Instrument Directory (${filteredStocks.size} Symbols)",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List
        if (filteredStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Matching Instruments", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Try searching for Reliance, TCS, HDFC Bank, or Infosys", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStocks) { stock ->
                    val isFav = watchlist.any { it.symbol == stock.symbol }
                    StockCard(
                        stock = stock,
                        onClick = { onNavigateToStockDetail(stock.symbol) },
                        isFavorite = isFav,
                        onFavoriteToggle = { viewModel.toggleWatchlist(stock) }
                    )
                }
            }
        }
    }
}
