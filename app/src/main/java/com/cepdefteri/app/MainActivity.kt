package com.cepdefteri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val turkish = Locale.forLanguageTag("tr-TR")
private fun money(kurus: Long) = NumberFormat.getCurrencyInstance(turkish).format(java.math.BigDecimal.valueOf(kurus, 2))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF166B56), secondaryContainer = Color(0xFFE2F4EB), background = Color(0xFFF6F8F5))) {
                ExpenseScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(vm: ExpenseViewModel = viewModel()) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf("Tümü") }
    var deleting by remember { mutableStateOf<Expense?>(null) }
    val month = YearMonth.now()
    val summary = monthlySummary(expenses, month)
    val visible = filterExpenses(expenses, filter)
    Scaffold(
        topBar = { TopAppBar(title = { Text("CepDefteri", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { adding = true }) { Text("+ Harcama ekle") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", turkish)), color = Color.White)
                        Text(money(summary.totalKurus), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Bu ay toplam • ${summary.count} harcama", color = Color.White)
                    }
                }
            }
            item { Text("Harcamalar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Tümü") + categories) { category ->
                        FilterChip(selected = category == filter, onClick = { filter = category }, label = { Text(category) })
                    }
                }
            }
            if (visible.isEmpty()) item {
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Henüz harcama yok", style = MaterialTheme.typography.titleMedium)
                    Text("İlk harcamanı ekle, paranı nereye harcadığını gör.")
                }
            }
            items(visible, key = { it.id }) { expense ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(expense.category, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(money(expense.amountKurus), fontWeight = FontWeight.Bold)
                        }
                        if (expense.note.isNotBlank()) Text(expense.note)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(LocalDate.ofEpochDay(expense.epochDay).format(DateTimeFormatter.ofPattern("dd MMM yyyy", turkish)), modifier = Modifier.padding(top = 12.dp))
                            TextButton(onClick = { deleting = expense }) { Text("Sil") }
                        }
                    }
                }
            }
        }
    }
    if (adding) AddExpenseDialog(onDismiss = { adding = false }, onSave = { amount, category, note, done -> vm.add(amount, category, note) { success -> if (success) adding = false; done() } })
    deleting?.let { expense ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Harcama silinsin mi?") }, text = { Text("${expense.category} • ${money(expense.amountKurus)}") }, confirmButton = { TextButton(onClick = { vm.delete(expense); deleting = null }) { Text("Sil") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Vazgeç") } })
    }
    error?.let { message -> AlertDialog(onDismissRequest = { vm.clearError() }, title = { Text("İşlem tamamlanamadı") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("Tamam") } }) }
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (Long, String, String, () -> Unit) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var submitted by remember { mutableStateOf(false) }
    val parsed = parseAmount(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni harcama") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Tutar (₺)") }, placeholder = { Text("Örn. 125,50") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = amount.isNotEmpty() && parsed == null, supportingText = { if (amount.isNotEmpty() && parsed == null) Text("Pozitif bir tutar girin (en fazla 2 ondalık).") })
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(categories) { item -> FilterChip(selected = item == category, onClick = { category = item }, label = { Text(item) }) } }
                OutlinedTextField(value = note, onValueChange = { note = it.take(200) }, label = { Text("Açıklama (isteğe bağlı)") }, maxLines = 3)
                Text("Bugünün tarihiyle kaydedilir. Veriler bu telefonda saklanır.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(enabled = parsed != null && !submitted, onClick = { parsed?.let { submitted = true; onSave(it, category, note) { submitted = false } } }) { Text("Kaydet") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}
