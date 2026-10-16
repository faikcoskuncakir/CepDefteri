package com.cepdefteri.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

val categories = listOf("Market", "Yemek", "Ulaşım", "Fatura", "Alışveriş", "Diğer")

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ExpenseDatabase.get(application).expenses()
    val expenses = dao.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    fun add(amount: Long, category: String, note: String, completed: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                dao.insert(Expense(amountKurus = amount, category = category, note = note.trim(), epochDay = LocalDate.now().toEpochDay()))
                completed(true)
            } catch (_: Exception) { _error.value = "Harcama kaydedilemedi. Tekrar deneyin."; completed(false) }
        }
    }
    fun delete(expense: Expense) { viewModelScope.launch {
        try { dao.delete(expense) }
        catch (_: Exception) { _error.value = "Harcama silinemedi. Tekrar deneyin." }
    } }
    fun clearError() { _error.value = null }
}
