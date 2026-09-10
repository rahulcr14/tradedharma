package com.tradedharma.app.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.components.money
import com.tradedharma.app.ui.components.displaySymbol
import android.graphics.BitmapFactory
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DETAIL_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(repository: TradeRepository, id: Long, onEdit: () -> Unit, onDeleted: () -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var trade by remember { mutableStateOf<TradeEntity?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(id) { trade = repository.get(id) }
    Scaffold(topBar = { TopAppBar(title = { Text("Trade Details") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }, actions = { trade?.let { t -> IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }; IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete") } } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val t = trade
            if (t == null) Text("Trade not found") else {
                Text(t.displaySymbol(), style = MaterialTheme.typography.headlineSmall)
                val selectedDate = t.tradeDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    ?: Date(t.openedAtEpochMs).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                Text("${selectedDate.format(DETAIL_DATE)} • ${t.direction.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Stat("Entry", "₹%.2f".format(t.entryPrice)); Stat("Exit", "₹%.2f".format(t.exitPrice)); Stat("Qty", "%.2f".format(t.quantity))
                }
                Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Net P&L", style = MaterialTheme.typography.labelLarge); Text(money(t.netPnl), style = MaterialTheme.typography.headlineMedium, color = if (t.netPnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); Text("Gross ${money(t.grossPnl)} • Charges ₹%.2f".format(t.charges)) } }
                Text("Risk / Reward", style = MaterialTheme.typography.titleMedium)
                Text("Planned R:R ${t.plannedRr?.let { "%.2f".format(it) } ?: "—"} • Actual R ${t.actualRMultiple?.let { "%.2fR".format(it) } ?: "—"}")
                Text("Strategy", style = MaterialTheme.typography.titleMedium); Text(t.strategy ?: "Unspecified")
                if (t.tags().isNotEmpty()) Text("Tags: ${t.tags().joinToString(" • ")}")
                Text("Notes", style = MaterialTheme.typography.titleMedium); Text(t.notes.ifBlank { "No notes added." })
                t.screenshotPath?.let { path -> if (File(path).exists()) { val bitmap = remember(path) { BitmapFactory.decodeFile(path) }; Text("Chart", style = MaterialTheme.typography.titleMedium); bitmap?.let { Image(it.asImageBitmap(), contentDescription = "Trade chart", modifier = Modifier.fillMaxWidth().height(220.dp)) } } }
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete trade?") }, text = { Text("This action cannot be undone.") }, confirmButton = { TextButton(onClick = { trade?.let { t -> scope.launch { repository.delete(t); confirmDelete = false; onDeleted() } } }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@Composable
private fun RowScope.Stat(label: String, value: String) { Column(Modifier.weight(1f)) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.titleMedium) } }
