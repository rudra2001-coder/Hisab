package com.rudra.hisab.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    fun format(amount: Double, bangla: Boolean = false): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }.format(amount)

        val numeral = if (bangla) BanglaNumberConverter.toBangla(formatted) else formatted
        return "\u09F3$numeral"
    }

    fun format(amount: Int, bangla: Boolean = false): String {
        return format(amount.toDouble(), bangla)
    }

    fun format(amount: Long, bangla: Boolean = false): String {
        return format(amount.toDouble(), bangla)
    }
}
