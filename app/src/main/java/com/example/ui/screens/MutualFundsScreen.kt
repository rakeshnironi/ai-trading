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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.MutualFund
import com.example.data.model.MutualFundCategory
import com.example.data.model.RiskLevel
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MutualFundsScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPortfolio: () -> Unit
) {
    val mutualFunds by viewModel.mutualFunds.collectAsState()
    val selectedCategory by viewModel.selectedMfCategory.collectAsState()
    val searchQuery by viewModel.mfSearchQuery.collectAsState()
    val selectedFund by viewModel.selectedMutualFund.collectAsState()
    val sipResult by viewModel.sipCalculationResult.collectAsState()

    var showSipSheet by remember { mutableStateOf(false) }
    var showInvestDialog by remember { mutableStateOf(false) }
    var activeFundForAction by remember { mutableStateOf<MutualFund?>(null) }
    var investAmount by remember { mutableDoubleStateOf(5000.0) }
    var isSipMode by remember { mutableStateOf(true) }

    val categories = MutualFundCategory.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("mutual_funds_screen")
    ) {
        // 1. Top Bar
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
                    modifier = Modifier.testTag("mf_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Mutual Funds & SIP",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Curated Indian Direct Growth Plans",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = {
                    activeFundForAction = selectedFund ?: mutualFunds.firstOrNull()
                    showSipSheet = true
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NavyCard)
                    .testTag("mf_sip_calculator_header_btn")
            ) {
                Icon(Icons.Default.Calculate, contentDescription = "SIP Calculator", tint = CyanAccent)
            }
        }

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateMfSearchQuery(it) },
            placeholder = { Text("Search Mutual Funds, Fund Houses...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateMfSearchQuery("") }) {
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("mf_search_input")
        )

        // 3. Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setMfCategory(category) },
                    label = { Text(category.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = NavyDeep,
                        containerColor = NavyCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = NavyBorder,
                        selectedBorderColor = CyanAccent
                    )
                )
            }
        }

        // 4. Content List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Featured Top Return Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SleekDarkPurple, NavyCardElevated)
                            )
                        )
                        .border(1.dp, AiCardBorder, RoundedCornerShape(16.dp))
                        .testTag("mf_hero_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CyanAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "HIGHEST 3Y CAGR",
                                    color = CyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quant Small Cap Direct-Growth",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AUM: ₹24,800 Cr • 3Y Return: +32.6% p.a. • NAV: ₹254.18",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeFundForAction = mutualFunds.find { it.id == "mf_quant_small" } ?: mutualFunds.firstOrNull()
                                    investAmount = 5000.0
                                    isSipMode = true
                                    showInvestDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("mf_hero_start_sip_btn")
                            ) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start ₹5k SIP", color = NavyDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    activeFundForAction = mutualFunds.find { it.id == "mf_quant_small" } ?: mutualFunds.firstOrNull()
                                    showSipSheet = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekLavender),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculate", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Mutual Funds List Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Schemes (${mutualFunds.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Direct Plans",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Funds List
            items(mutualFunds, key = { it.id }) { fund ->
                MutualFundItemCard(
                    fund = fund,
                    onSelect = { viewModel.selectMutualFund(fund) },
                    onStartSip = {
                        activeFundForAction = fund
                        investAmount = fund.minSipAmount
                        isSipMode = true
                        showInvestDialog = true
                    },
                    onOpenCalculator = {
                        activeFundForAction = fund
                        viewModel.selectMutualFund(fund)
                        showSipSheet = true
                    }
                )
            }
        }
    }

    // SIP Calculator Modal Bottom Sheet
    if (showSipSheet) {
        val targetFund = activeFundForAction ?: selectedFund ?: mutualFunds.firstOrNull()
        var monthlySip by remember { mutableDoubleStateOf(5000.0) }
        var tenureYears by remember { mutableIntStateOf(5) }
        var expectedCagr by remember { mutableDoubleStateOf(targetFund?.threeYearReturn ?: 15.0) }

        val calculated = remember(monthlySip, tenureYears, expectedCagr) {
            viewModel.updateSipParams(monthlySip, tenureYears, expectedCagr)
            val n = tenureYears * 12
            val i = (expectedCagr / 100.0) / 12.0
            val totalInv = monthlySip * n
            val matVal = if (i > 0) monthlySip * ((Math.pow(1.0 + i, n.toDouble()) - 1.0) / i) * (1.0 + i) else totalInv
            val gains = Math.max(0.0, matVal - totalInv)
            Triple(totalInv, gains, matVal)
        }

        ModalBottomSheet(
            onDismissRequest = { showSipSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = NavySurface,
            modifier = Modifier.testTag("sip_calculator_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SIP Wealth Forecaster",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        targetFund?.let {
                            Text(text = it.name, color = CyanAccent, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    IconButton(onClick = { showSipSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Output Result Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NavyBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Estimated Maturity Value", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = "₹${String.format(Locale.US, "%,.0f", calculated.third)}",
                            color = EmeraldGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Invested", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.0f", calculated.first)}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Estimated Gains", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = "+₹${String.format(Locale.US, "%,.0f", calculated.second)}",
                                    color = CyanAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sliders
                Text(
                    text = "Monthly SIP: ₹${String.format(Locale.US, "%,.0f", monthlySip)}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = monthlySip.toFloat(),
                    onValueChange = { monthlySip = (it / 500).toInt() * 500.0 },
                    valueRange = 500f..50000f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                )

                Text(
                    text = "Tenure: $tenureYears Years (${tenureYears * 12} Installments)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = tenureYears.toFloat(),
                    onValueChange = { tenureYears = it.toInt() },
                    valueRange = 1f..25f,
                    colors = SliderDefaults.colors(thumbColor = SleekLavender, activeTrackColor = SleekLavender)
                )

                Text(
                    text = "Expected Annual Return: ${String.format(Locale.US, "%.1f", expectedCagr)}% p.a.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = expectedCagr.toFloat(),
                    onValueChange = { expectedCagr = it.toDouble() },
                    valueRange = 6f..35f,
                    colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showSipSheet = false
                        targetFund?.let {
                            activeFundForAction = it
                            investAmount = monthlySip
                            isSipMode = true
                            showInvestDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("sip_confirm_and_invest_btn")
                ) {
                    Text(
                        text = "Invest ₹${String.format(Locale.US, "%,.0f", monthlySip)} / Month in Paper Portfolio",
                        color = NavyDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Virtual Investment Execution Dialog
    if (showInvestDialog && activeFundForAction != null) {
        val fund = activeFundForAction!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInvestDialog = false },
            containerColor = NavySurface,
            title = {
                Text(
                    text = if (isSipMode) "Start Mutual Fund SIP" else "Lumpsum Investment",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = fund.name, color = CyanAccent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "Current NAV: ₹${fund.nav} • Rating: ${fund.starRating}★", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = investAmount.toInt().toString(),
                        onValueChange = { investAmount = it.toDoubleOrNull() ?: fund.minSipAmount },
                        label = { Text("Investment Amount (₹)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1000.0, 2500.0, 5000.0, 10000.0).forEach { amt ->
                            Surface(
                                color = if (investAmount == amt) CyanAccent.copy(alpha = 0.2f) else NavyCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (investAmount == amt) CyanAccent else NavyBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { investAmount = amt }
                            ) {
                                Text(
                                    text = "₹${amt.toInt()}",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.investInMutualFund(fund, investAmount, isSipMode)
                        showInvestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Confirm Paper Investment", color = NavyDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInvestDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun MutualFundItemCard(
    fund: MutualFund,
    onSelect: () -> Unit,
    onStartSip: () -> Unit,
    onOpenCalculator: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("mf_item_${fund.id}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Fund House & Rating & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = SleekDarkPurple.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = fund.category.displayName,
                            color = SleekLavender,
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
                            text = fund.fundHouse,
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${fund.starRating}.0",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fund Name & NAV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = fund.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = fund.formattedNav,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = fund.formattedDayChange,
                        color = if (fund.isPositive) EmeraldGreen else CrimsonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Return Horizon Metrics Grid
            Surface(
                color = NavyDeep.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "1Y Return", color = TextMuted, fontSize = 10.sp)
                        Text(text = fund.formatted1Y, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "3Y CAGR", color = TextMuted, fontSize = 10.sp)
                        Text(text = fund.formatted3Y, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "5Y CAGR", color = TextMuted, fontSize = 10.sp)
                        Text(text = fund.formatted5Y, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Expense", color = TextMuted, fontSize = 10.sp)
                        Text(text = "${fund.expenseRatio}%", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenCalculator,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SIP Calculator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onStartSip,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invest / SIP", color = NavyDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
