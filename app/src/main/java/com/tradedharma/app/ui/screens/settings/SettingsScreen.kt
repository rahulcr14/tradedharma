package com.tradedharma.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tradedharma.app.data.export.TradeExporter
import com.tradedharma.app.data.export.TradeImporter
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(repository: TradeRepository, onDailyReview: () -> Unit, onWeeklyReview: () -> Unit, themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    val trades by repository.observeTrades().collectAsState(initial = emptyList())
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null) scope.launch { writeText(context, uri, TradeExporter.csv(trades)); status = "CSV exported" } }
    val createJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) scope.launch { writeText(context, uri, TradeExporter.json(trades)); status = "JSON exported" } }
    val openAny = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) scope.launch { runCatching { val text = context.contentResolver.openInputStream(uri)!!.bufferedReader().readText(); val isCsv = (context.contentResolver.getType(uri).orEmpty().contains("csv") || uri.toString().lowercase().endsWith(".csv")); val imported = if (isCsv) TradeImporter.csv(text) else TradeImporter.json(text); repository.insertTradesTransactional(imported); status = "Imported ${imported.size} trades" }.onFailure { status = "Import failed: ${it.message}" } } }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("More", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text("Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Button(onClick = { createDocument.launch("tradelog-export.csv") }, Modifier.fillMaxWidth()) { Text("Export CSV") }
        Button(onClick = { createJson.launch("tradelog-backup.json") }, Modifier.fillMaxWidth()) { Text("Export JSON backup") }
        OutlinedButton(onClick = { openAny.launch(arrayOf("application/json", "text/csv", "text/comma-separated-values", "text/*")) }, Modifier.fillMaxWidth()) { Text("Import CSV / JSON backup") }
        HorizontalDivider()
        Text("Reviews", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        OutlinedButton(onClick = onDailyReview, Modifier.fillMaxWidth()) { Text("Daily review") }
        OutlinedButton(onClick = onWeeklyReview, Modifier.fillMaxWidth()) { Text("Weekly review") }
        HorizontalDivider()
        Text("Theme", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.values().forEach { mode ->
                FilterChip(selected = themeMode == mode, onClick = { onThemeChange(mode) }, label = { Text(mode.label()) })
            }
        }
        HorizontalDivider()
        Text("Open Source", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("TradeDharma is designed as a privacy-first, offline-first open-source trading journal. Core trade data stays on the device until you export it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        status?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}

private suspend fun writeText(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.AMOLED_BLACK -> "AMOLED Black"
}
