package com.afquintana.buycheaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.afquintana.buycheaper.presentation.navigation.BuyCheaperNavHost
import com.afquintana.buycheaper.presentation.theme.BuyCheaperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuyCheaperTheme {
                BuyCheaperNavHost()
            }
        }
    }
}
