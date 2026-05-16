package com.rudra.hisab.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.BANGLA }
val LocalStrings = staticCompositionLocalOf { Strings(AppLanguage.BANGLA) }

fun createStrings(lang: AppLanguage): Strings = Strings(lang)
fun appLanguageFromCode(code: String): AppLanguage = AppLanguage.fromCode(code)
