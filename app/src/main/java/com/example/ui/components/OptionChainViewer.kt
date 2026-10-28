package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.OptionChainData
import com.example.data.model.OptionStrike
import com.example.data.model.OrderProductType
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
import java.util.Locale

@Composable
fun OptionChainModal(
    symbol: String,
    viewModel: TradingViewModel,
    onDismiss: () -> Unit
) {
    LaunchedEffect(symbol) {
        viewModel.loadOptionChain(symbol)
    }

    val chainData by viewModel.selectedOptionChain.collectAsState()
    var selectedOrderStrike by remember { mutableStateOf<Pair<OptionStrike, String>?>(null) } // Strike and "CE" or "PE"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCardElevated,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("option_chain_modal"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$symbol Option Chain",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = SleekLavender.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text("F&O Live", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekLavender, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    chainData?.let {
                        Text(
                            text = "Spot: ₹${String.format(Locale.US, "%.2f", it.underlyingLtp)} • PCR: ${String.format(Locale.US, "%.2f", it.pcrRatio)} • Max Pain: ₹${String.format(Locale.US, "%.0f", it.maxPain)}",
                            fontSize = 11.sp,
                            color = CyanAccent
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            val chain = chainData
            if (chain == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Loading live option contracts...", color = TextSecondary)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Expiry Date Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chain.availableExpiries) { exp ->
                            FilterChip(
                                selected = chain.expiryDate == exp,
                                onClick = { viewModel.loadOptionChain(symbol, exp) },
                                label = { Text(exp, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekLavender,
                                    selectedLabelColor = NavyDeep
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option Chain Table Header
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CALLS (CE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.weight(1f))
                            Text("STRIKE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekLavender, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text("PUTS (PE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonRed, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Strikes LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(chain.strikes) { strike ->
                            val isAtm = strike.isAtm
                            val rowBackground = when {
                                isAtm -> SleekLavender.copy(alpha = 0.15f)
                                strike.isItmCall -> EmeraldGreen.copy(alpha = 0.05f)
                                strike.isItmPut -> CrimsonRed.copy(alpha = 0.05f)
                                else -> Color.Transparent
                            }

                            Surface(
                                color = rowBackground,
                                shape = RoundedCornerShape(6.dp),
                                border = if (isAtm) BorderStroke(1.dp, SleekLavender) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // CALLS SIDE
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedOrderStrike = Pair(strike, "CE") }
                                    ) {
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.1f", strike.callLtp)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Text(
                                            text = "OI: ${(strike.callOi / 1000)}k",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    // STRIKE (CENTER)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.0f", strike.strikePrice),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isAtm) AmberGold else TextPrimary
                                        )
                                        if (isAtm) {
                                            Text("ATM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                        }
                                    }

                                    // PUTS SIDE
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedOrderStrike = Pair(strike, "PE") },
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.1f", strike.putLtp)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CrimsonRed
                                        )
                                        Text(
                                            text = "OI: ${(strike.putOi / 1000)}k",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Close", color = TextSecondary)
            }
        }
    )

    // Fast Options Order Placement Confirmation
    selectedOrderStrike?.let { (strike, optType) ->
        val price = if (optType == "CE") strike.callLtp else strike.putLtp
        AlertDialog(
            onDismissRequest = { selectedOrderStrike = null },
            containerColor = NavyCardElevated,
            title = {
                Text("Trade $symbol ${strike.strikePrice.toInt()} $optType", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Premium: ₹${String.format(Locale.US, "%.2f", price)} per share", fontSize = 13.sp, color = CyanAccent)
                    Text("Standard Lot Size: 50 units (Estimated Margin: ₹${String.format(Locale.US, "%,.2f", price * 50)})", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.placeAngelOneOrder(
                            symbol = "$symbol ${strike.strikePrice.toInt()} $optType",
                            action = SignalType.BUY,
                            orderType = OrderType.MARKET,
                            productType = OrderProductType.INTRADAY,
                            quantity = 50,
                            price = price
                        )
                        selectedOrderStrike = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buy 1 Lot ($optType)", color = NavyDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedOrderStrike = null }, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
