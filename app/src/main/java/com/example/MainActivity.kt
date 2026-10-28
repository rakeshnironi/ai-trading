package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppThemeProvider
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.domain.ml.LSTMPredictionEngine.initContext(applicationContext)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            AppThemeProvider(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NavyDeep)
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
