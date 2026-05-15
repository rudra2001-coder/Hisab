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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class FontSizeScale(val scale: Float, val label: String) {
    SMALL(0.85f, "ছোট"),
    MEDIUM(1.0f, "মধ্যম"),
    LARGE(1.15f, "বড়")
}

object HisabThemeConfig {
    var fontScale: FontSizeScale = FontSizeScale.MEDIUM
    var density: Dp = 0.dp
}

@Composable
fun HisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "system",
    fontSize: String = "medium",
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val scale = when (fontSize) {
        "small" -> FontSizeScale.SMALL
        "large" -> FontSizeScale.LARGE
        else -> FontSizeScale.MEDIUM
    }
    HisabThemeConfig.fontScale = scale

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HisabTypography,
        shapes = HisabShapes,
        content = content
    )
}

fun scaledSize(sp: Int): androidx.compose.ui.unit.TextUnit {
    return (sp * HisabThemeConfig.fontScale.scale).sp
}
