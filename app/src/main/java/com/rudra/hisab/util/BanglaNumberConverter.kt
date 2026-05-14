package com.rudra.hisab.util

object BanglaNumberConverter {

    private val banglaDigits = arrayOf(
        '\u09E6', '\u09E7', '\u09E8', '\u09E9', '\u09EA',
        '\u09EB', '\u09EC', '\u09ED', '\u09EE', '\u09EF'
    )

    fun toBangla(number: String): String {
        return number.map { char ->
            if (char.isDigit()) banglaDigits[char - '0'] else char
        }.joinToString("")
    }

    fun toBangla(number: Int): String {
        return toBangla(number.toString())
    }

    fun toBangla(number: Long): String {
        return toBangla(number.toString())
    }

    fun toBangla(number: Double): String {
        val formatted = String.format("%.2f", number)
        return toBangla(formatted)
    }
}
