package com.rudra.hisab.util

enum class AppLanguage(val code: String) {
    BANGLA("bn"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BANGLA
    }
}
