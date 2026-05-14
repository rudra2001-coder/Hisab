package com.rudra.hisab.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.staticCompositionLocalOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class LanguageState(
    val isBangla: Boolean = true
)

val LocalLanguageState = staticCompositionLocalOf { LanguageState() }

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getLocale(isBangla: Boolean): Locale {
        return if (isBangla) Locale("bn") else Locale("en")
    }

    fun updateLocale(isBangla: Boolean): Context {
        val locale = getLocale(isBangla)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
