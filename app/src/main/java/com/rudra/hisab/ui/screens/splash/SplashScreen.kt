package com.rudra.hisab.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.rudra.hisab.data.preferences.AppSettings
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    settings: AppSettings,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLock: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    LaunchedEffect(settings) {
        val delay = when {
            !settings.hasCompletedOnboarding -> 1500L
            settings.isPinEnabled -> 500L
            else -> 1000L
        }
        delay(delay)
        when {
            !settings.hasCompletedOnboarding -> onNavigateToOnboarding()
            settings.isPinEnabled -> onNavigateToLock()
            else -> onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "হিসাব",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
