package com.rudra.hisab.ui.screens.lock

import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.rudra.hisab.data.preferences.AppSettings
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.util.concurrent.Executors

@Composable
fun LockScreen(
    settings: AppSettings,
    onUnlock: () -> Unit,
    onExitApp: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockedUntil by remember { mutableLongStateOf(0L) }

    val context = LocalContext.current

    BackHandler {
        onExitApp()
    }

    LaunchedEffect(Unit) {
        if (settings.isBiometricEnabled) {
            try {
                val biometricManager = BiometricManager.from(context)
                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
                    val executor = Executors.newSingleThreadExecutor()
                    val biometricPrompt = BiometricPrompt(
                        context as FragmentActivity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                onUnlock()
                            }
                        }
                    )
                    biometricPrompt.authenticate(
                        BiometricPrompt.PromptInfo.Builder()
                            .setTitle("হিসাব")
                            .setSubtitle("বায়োমেট্রিক যাচাইকরণ")
                            .setNegativeButtonText("পিন ব্যবহার করুন")
                            .build()
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(isLocked, lockedUntil) {
        if (isLocked) {
            val remaining = lockedUntil - System.currentTimeMillis()
            if (remaining > 0) {
                delay(remaining)
            }
            isLocked = false
            attempts = 0
        }
    }

    fun verifyPin() {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
        if (settings.pinHash.isNotEmpty() && hash == settings.pinHash) {
            onUnlock()
        } else if (settings.pinHash.isNotEmpty() && pin == settings.pinHash) {
            onUnlock()
        } else {
            attempts++
            if (attempts >= 3) {
                isLocked = true
                lockedUntil = System.currentTimeMillis() + 30000
                error = "৩০ সেকেন্ড অপেক্ষা করুন"
            } else {
                error = "ভুল পিন"
            }
            pin = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "হিসাব",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (!isLocked && it.length <= 4 && it.all { c -> c.isDigit() }) {
                    pin = it
                    error = null
                    if (it.length == 4) {
                        verifyPin()
                    }
                }
            },
            label = { Text("পিন") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            enabled = !isLocked,
            modifier = Modifier.width(200.dp),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center)
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
