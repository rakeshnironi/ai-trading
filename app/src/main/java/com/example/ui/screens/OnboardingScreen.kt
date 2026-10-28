package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "AI-Powered Stock Prediction",
            subtitle = "Deep Learning Neural Forecasting",
            description = "Harness bidirectional LSTM networks and temporal attention models trained exclusively on Angel One historical OHLCV data to project multi-horizon price targets.",
            icon = Icons.Default.Psychology,
            accentColor = CyanAccent
        ),
        OnboardingPageData(
            title = "Paper Trading with Virtual Money",
            subtitle = "₹1,00,000 Starting Virtual Balance",
            description = "Practice trading Indian equities risk-free using live Angel One SmartAPI tick prices. Experience authentic execution, portfolio tracking, and instant WhatsApp trade logs.",
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = EmeraldGreen
        ),
        OnboardingPageData(
            title = "Machine Learning BUY / SELL / HOLD Signals",
            subtitle = "Multi-Factor Technical Consensus",
            description = "Receive algorithmic recommendations synthesized from RSI, MACD, Bollinger Bands, and SuperTrend with transparent reasoning and confidence scores.",
            icon = Icons.AutoMirrored.Filled.ShowChart,
            accentColor = AmberGold
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavyCardElevated
                ) {
                    Text(
                        text = "ANGEL ONE SMARTAPI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.testTag("skip_button")
                    ) {
                        Text("Skip", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Feature Icon Art
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, page.accentColor.copy(alpha = 0.3f)),
                        modifier = Modifier.size(140.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                tint = page.accentColor,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = page.subtitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = page.accentColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .size(width = if (isSelected) 24.dp else 6.dp, height = 6.dp)
                            .background(
                                color = if (isSelected) CyanAccent else NavyBorder,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Buttons (Next / Get Started)
            if (pagerState.currentPage == pages.size - 1) {
                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("get_started_button")
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDeep
                    )
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDeep
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NavyDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
