package com.afquintana.buycheaper.presentation.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val Blue = Color(0xFF2563EB)

private val BuyCheaperDarkColorScheme = darkColorScheme(
    primary = Blue,
    secondary = Blue,
    tertiary = Blue
)

private val BuyCheaperLightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Blue,
    tertiary = Blue
)

@Composable
fun BuyCheaperTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        BuyCheaperDarkColorScheme
    } else {
        BuyCheaperLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
