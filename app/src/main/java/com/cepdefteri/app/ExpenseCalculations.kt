package com.cepdefteri.app

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

// Integer kuruş storage avoids floating point rounding in money calculations.
fun parseAmount(text: String): Long? = try {
    val normalized = text.trim().replace(',', '.')
    if (!Regex("[0-9]{1,9}(\\.[0-9]{1,2})?").matches(normalized)) null
    else BigDecimal(normalized).movePointRight(2).longValueExact().takeIf { it > 0 }
} catch (_: Exception) { null }

data class MonthlySummary(val totalKurus: Long, val count: Int)

fun monthlySummary(expenses: List<Expense>, month: YearMonth): MonthlySummary {
    val matching = expenses.filter { YearMonth.from(LocalDate.ofEpochDay(it.epochDay)) == month }
    return MonthlySummary(matching.sumOf { it.amountKurus }, matching.size)
}

fun filterExpenses(expenses: List<Expense>, category: String): List<Expense> =
    expenses.filter { category == "Tümü" || it.category == category }
