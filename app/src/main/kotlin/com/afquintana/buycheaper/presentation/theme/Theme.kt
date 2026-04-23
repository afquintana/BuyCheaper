package com.afquintana.buycheaper.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BuyCheaperColorScheme = darkColorScheme()

@Composable
fun BuyCheaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BuyCheaperColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
