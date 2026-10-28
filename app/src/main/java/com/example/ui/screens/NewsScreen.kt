package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMarketTrendSummary
import com.example.data.model.MacroDriverItem
import com.example.data.model.NewsItem
import com.example.data.model.SectorTrendItem
import com.example.data.model.SignalType
import com.example.ui.components.SignalBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
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
fun NewsScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: ((String) -> Unit)? = null
) {
    val newsList by viewModel.newsList.collectAsState()
    val geminiSummary by viewModel.geminiDailySummary.collectAsState()
    val isGeneratingSummary by viewModel.isGeneratingDailySummary.collectAsState()
    val selectedCategory by viewModel.selectedNewsCategory.collectAsState()
    val selectedSentiment by viewModel.selectedNewsSentiment.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedNewsIds.collectAsState()

    val marketAiAnswer by viewModel.marketAiAnswer.collectAsState()
    val isAnsweringAi by viewModel.isAnsweringMarketAi.collectAsState()

    var customQuestionText by remember { mutableStateOf("") }
    var isBriefingExpanded by remember { mutableStateOf(true) }
    var showAiAskBox by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "ALL" to "All Feeds",
        "Markets" to "Indices & Equities",
        "Banking & Economy" to "Banking & RBI",
        "IT & Tech" to "IT & AI",
        "Automobile" to "Auto & EV",
        "Commodities" to "Crude & Gold",
        "Economy" to "Macro Economy",
        "Pharma" to "Pharma & Health"
    )

    val filteredNews = newsList.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSentiment = selectedSentiment == null || item.sentiment == selectedSentiment
        matchesCategory && matchesSentiment
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("market_news_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Header with Refresh Action
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Market Intelligence",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekLavender.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini AI",
                                        tint = SleekLavender,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Gemini 3.5",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekLavender
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily Trend Summary & Financial Feeds",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.fetchGeminiDailyMarketSummary(forceRefresh = true) },
                        enabled = !isGeneratingSummary,
                        modifier = Modifier.testTag("refresh_gemini_summary_button")
                    ) {
                        if (isGeneratingSummary) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SleekLavender)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Gemini Analysis",
                                tint = SleekLavender
                            )
                        }
                    }
                }
            }
        }

        // 2. Gemini Daily Financial Market Trends Summary Hero Card
        item {
            geminiSummary?.let { summary ->
                GeminiDailyExecutiveCard(
                    summary = summary,
                    isExpanded = isBriefingExpanded,
                    onToggleExpand = { isBriefingExpanded = !isBriefingExpanded },
                    onAskAiClick = { showAiAskBox = !showAiAskBox }
                )
            } ?: run {
                if (isGeneratingSummary) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = SleekLavender, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Column {
                                Text("Synthesizing Daily Market Trends...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Gemini 3.5 Flash is analyzing Nifty 50, macroeconomic drivers & live news.", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive "Ask Gemini Market AI" Drawer
        item {
            AnimatedVisibility(
                visible = showAiAskBox,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Ask AI", tint = AmberGold, modifier = Modifier.size(18.dp))
                                Text("Ask Gemini Market AI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            IconButton(onClick = {
                                showAiAskBox = false
                                viewModel.clearMarketAiAnswer()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Sample Quick Prompt Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Why is Nifty rising today?",
                                "Impact of crude oil on paints",
                                "IT sector outlook today",
                                "Bank Nifty support & resistance"
                            ).forEach { chipPrompt ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = NavyCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.clickable {
                                        customQuestionText = chipPrompt
                                        viewModel.askMarketAi(chipPrompt)
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Text(
                                        text = chipPrompt,
                                        fontSize = 11.sp,
                                        color = SleekLavender,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Query Input Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customQuestionText,
                                onValueChange = { customQuestionText = it },
                                placeholder = { Text("Ask about market trends or sector impacts...", fontSize = 12.sp, color = TextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("market_ai_input_field"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (customQuestionText.isNotBlank()) {
                                        viewModel.askMarketAi(customQuestionText)
                                        focusManager.clearFocus()
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekLavender,
                                    unfocusedBorderColor = NavyBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = SleekLavender
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (customQuestionText.isNotBlank()) {
                                        viewModel.askMarketAi(customQuestionText)
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = customQuestionText.isNotBlank() && !isAnsweringAi,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("market_ai_send_button")
                            ) {
                                if (isAnsweringAi) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NavyDeep, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NavyDeep, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // AI Response Card
                        marketAiAnswer?.let { answer ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = NavyCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                                        Text("Gemini 3.5 Market Intelligence Answer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = answer,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Category & Sentiment Filters
        item {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) SleekLavender else NavyCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SleekLavender else NavyBorder
                            ),
                            modifier = Modifier
                                .clickable { viewModel.setNewsCategory(catKey) }
                                .testTag("news_cat_${catKey.lowercase()}")
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NavyDeep else TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sentiment Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sentiment:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)

                    listOf(
                        null to "All",
                        SignalType.BUY to "Bullish",
                        SignalType.HOLD to "Neutral",
                        SignalType.SELL to "Bearish"
                    ).forEach { (signal, label) ->
                        val isSelected = selectedSentiment == signal
                        val chipColor = when (signal) {
                            SignalType.BUY -> EmeraldGreen
                            SignalType.SELL -> CrimsonRed
                            SignalType.HOLD -> AmberGold
                            null -> SleekLavender
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else NavyCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) chipColor else NavyBorder
                            ),
                            modifier = Modifier.clickable { viewModel.setNewsSentiment(signal) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) chipColor else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Section Title & Item Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Financial News (${filteredNews.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-Time AI Summarized",
                    fontSize = 11.sp,
                    color = SleekLavender
                )
            }
        }

        // 6. News Items List
        if (filteredNews.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No news matching this filter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Try selecting 'All Feeds' or resetting the sentiment filter.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredNews, key = { it.id }) { item ->
                val isBookmarked = bookmarkedIds.contains(item.id)
                NewsFeedItemCard(
                    news = item,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = { viewModel.toggleBookmarkNews(item.id) },
                    onStockClick = { symbol -> onNavigateToStockDetail?.invoke(symbol) }
                )
            }
        }
    }
}

@Composable
private fun GeminiDailyExecutiveCard(
    summary: DailyMarketTrendSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAskAiClick: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            NavyCard,
            NavySurface.copy(alpha = 0.95f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("gemini_daily_summary_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Badge & Sentiment Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = SleekLavender.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text("Daily Market Executive Briefing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val sentimentColor = when (summary.overallSentiment) {
                        SignalType.BUY -> EmeraldGreen
                        SignalType.SELL -> CrimsonRed
                        SignalType.HOLD -> AmberGold
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = sentimentColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, sentimentColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val icon = when (summary.overallSentiment) {
                                SignalType.BUY -> Icons.AutoMirrored.Filled.TrendingUp
                                SignalType.SELL -> Icons.AutoMirrored.Filled.TrendingDown
                                SignalType.HOLD -> Icons.AutoMirrored.Filled.TrendingFlat
                            }
                            Icon(icon, contentDescription = null, tint = sentimentColor, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${summary.sentimentScore}% ${if (summary.overallSentiment == SignalType.BUY) "Bullish" else if (summary.overallSentiment == SignalType.SELL) "Bearish" else "Neutral"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = sentimentColor
                            )
                        }
                    }
                }
            }

            // Headline
            Text(
                text = summary.headline,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            // Executive Summary Paragraph
            Text(
                text = summary.executiveBriefing,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp
            )

            // Expandable Content: Macro Drivers, Sectors, Key Pivot Levels & Takeaways
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Key Levels Pill Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NavyDeep,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nifty Support", fontSize = 10.sp, color = TextMuted)
                                Text("${summary.keyLevels.niftySupport.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nifty Resistance", fontSize = 10.sp, color = TextMuted)
                                Text("${summary.keyLevels.niftyResistance.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("India VIX", fontSize = 10.sp, color = TextMuted)
                                Text("${summary.keyLevels.vixLevel}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }
                    }

                    // Key Macro Drivers
                    if (summary.keyDrivers.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Key Market Drivers Today", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            summary.keyDrivers.forEach { driver ->
                                MacroDriverRow(driver)
                            }
                        }
                    }

                    // Sectoral Trends Heatmap
                    if (summary.sectorTrends.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Sectoral Rotation & Momentum", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            summary.sectorTrends.forEach { sector ->
                                SectorTrendRow(sector)
                            }
                        }
                    }

                    // AI Key Takeaways
                    if (summary.topTakeaways.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SleekLavender.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Gemini Actionable Takeaways", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                                summary.topTakeaways.forEach { takeaway ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = SleekLavender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(takeaway, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Actions: Toggle Expand & Ask AI CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SleekLavender.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { onAskAiClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                        Text("Ask Market AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                    }
                }

                Row(
                    modifier = Modifier.clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Less Details" else "View Detailed Breakdown",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroDriverRow(driver: MacroDriverItem) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = NavyDeep,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(driver.description, fontSize = 10.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (driver.impact.contains("Positive", ignoreCase = true)) EmeraldGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f)
            ) {
                Text(
                    text = driver.impact,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (driver.impact.contains("Positive", ignoreCase = true)) EmeraldGreen else AmberGold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SectorTrendRow(sector: SectorTrendItem) {
    val color = when (sector.trend) {
        SignalType.BUY -> EmeraldGreen
        SignalType.SELL -> CrimsonRed
        SignalType.HOLD -> AmberGold
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = NavyDeep,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(sector.sectorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "${if (sector.changePercent >= 0) "+" else ""}${String.format("%.2f", sector.changePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Text(sector.keyDriver, fontSize = 10.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            SignalBadge(signal = sector.trend)
        }
    }
}

@Composable
private fun NewsFeedItemCard(
    news: NewsItem,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onStockClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("news_card_${news.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Category pill, Source & Sentiment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = news.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text("•", color = TextMuted, fontSize = 10.sp)
                    Text(news.source, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(news.timeAgo, fontSize = 10.sp, color = TextMuted)
                    SignalBadge(signal = news.sentiment)
                }
            }

            // Headline
            Text(
                text = news.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            // News Summary
            Text(
                text = news.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            // Gemini AI Quick Takeaway Box
            if (news.aiTakeaway.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SleekLavender.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekLavender,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Gemini Key Takeaway",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekLavender
                            )
                        }
                        Text(
                            text = news.aiTakeaway,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Trader Impact Note
            if (news.traderImpact.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Trader Impact:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    Text(
                        text = news.traderImpact,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        lineHeight = 15.sp
                    )
                }
            }

            // Bottom Actions: Related Stock Button & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!news.relatedSymbol.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.clickable { onStockClick(news.relatedSymbol) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                            Text("Analyze ${news.relatedSymbol}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (isBookmarked) AmberGold else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
