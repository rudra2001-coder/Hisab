package com.rudra.hisab.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenProfit,
    onPrimary = Color.White,
    primaryContainer = GreenProfitContainer,
    onPrimaryContainer = GreenProfitOnContainer,
    secondary = OrangeDue,
    onSecondary = Color.White,
    secondaryContainer = OrangeDueContainer,
    onSecondaryContainer = OrangeDueOnContainer,
    error = RedExpense,
    onError = Color.White,
    errorContainer = RedExpenseContainer,
    onErrorContainer = RedExpenseOnContainer,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenProfit,
    onPrimary = Color.White,
    primaryContainer = GreenProfitDark,
    onPrimaryContainer = GreenProfitContainer,
    secondary = OrangeDue,
    onSecondary = Color.White,
    secondaryContainer = OrangeDueDark,
    onSecondaryContainer = OrangeDueContainer,
    error = RedExpense,
    onError = Color.White,
    errorContainer = RedExpenseDark,
    onErrorContainer = RedExpenseContainer,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark,
    outline = OutlineDark
)

@Composable
fun HisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HisabTypography,
        shapes = HisabShapes,
        content = content
    )
}
