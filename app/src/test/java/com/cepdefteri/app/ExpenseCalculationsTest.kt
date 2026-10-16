package com.cepdefteri.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ExpenseCalculationsTest {
    @Test fun `whole lira converts to kurus`() { assertEquals(12500L, parseAmount("125")) }
    @Test fun `Turkish decimal separator is accepted`() { assertEquals(12550L, parseAmount("125,50")) }
    @Test fun `dot decimal separator is accepted`() { assertEquals(12550L, parseAmount("125.50")) }
    @Test fun `single decimal digit is padded`() { assertEquals(1250L, parseAmount("12,5")) }
    @Test fun `surrounding whitespace is ignored`() { assertEquals(12550L, parseAmount(" 125,50 ")) }
    @Test fun `smallest supported amount is one kurus`() { assertEquals(1L, parseAmount("0,01")) }
    @Test fun `maximum supported amount stays exact`() { assertEquals(99999999999L, parseAmount("999999999,99")) }
    @Test fun `zero and negative amounts are rejected`() {
        listOf("0", "0,00", "-1", "-0,01").forEach { assertNull(it, parseAmount(it)) }
    }
    @Test fun `malformed and ambiguous amounts are rejected`() {
        listOf("", " ", "abc", "1,234", "1.234,56", "1,2.3", "12.", ".50", "+1", "1e3", "1 000", "1000000000").forEach { assertNull(it, parseAmount(it)) }
    }
    private fun expense(id: Long, date: String, amount: Long, category: String = "Market") =
        Expense(id, amount, category, "", LocalDate.parse(date).toEpochDay())

    @Test fun `monthly summary includes first and last day and excludes neighboring months`() {
        val records = listOf(expense(1, "2026-09-30", 999), expense(2, "2026-10-01", 12550), expense(3, "2026-10-31", 1), expense(4, "2026-11-01", 888), expense(5, "2025-10-15", 777))
        assertEquals(MonthlySummary(12551, 2), monthlySummary(records, YearMonth.of(2026, 10)))
    }
    @Test fun `empty month has zero total and count`() {
        assertEquals(MonthlySummary(0, 0), monthlySummary(emptyList(), YearMonth.of(2026, 10)))
        assertEquals(MonthlySummary(0, 0), monthlySummary(listOf(expense(1, "2026-09-01", 500)), YearMonth.of(2026, 10)))
    }
    @Test fun `leap day belongs to February`() {
        assertEquals(MonthlySummary(50, 1), monthlySummary(listOf(expense(1, "2024-02-29", 50), expense(2, "2024-03-01", 100)), YearMonth.of(2024, 2)))
    }
    @Test fun `all filter keeps every record in original order`() {
        val records = listOf(expense(3, "2026-10-03", 1, "Yemek"), expense(1, "2026-10-01", 2))
        assertEquals(records, filterExpenses(records, "Tümü"))
    }
    @Test fun `category filter selects matching records preserving order`() {
        val records = listOf(expense(3, "2026-10-03", 1), expense(2, "2026-10-02", 2, "Yemek"), expense(1, "2026-10-01", 3))
        assertEquals(listOf(records[0], records[2]), filterExpenses(records, "Market"))
        assertTrue(filterExpenses(records, "Fatura").isEmpty())
    }
    @Test fun `filtering empty records returns empty list`() { assertTrue(filterExpenses(emptyList(), "Market").isEmpty()) }
}
