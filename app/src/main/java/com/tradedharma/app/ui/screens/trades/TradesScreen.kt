package com.tradedharma.app.ui.screens.trades

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.components.EmptyState
import com.tradedharma.app.ui.components.money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(repository: TradeRepository, onTradeClick: (Long) -> Unit) {
    val trades = repository.observeTrades().collectAsStateWithLifecycle(emptyList()).value
    var query by rememberSaveable { mutableStateOf("") }
    var instrument by rememberSaveable { mutableStateOf<InstrumentType?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    val filtered = trades.filter { t ->
        val matchesText = query.isBlank() || t.symbol.contains(query, true) || t.strategy.orEmpty().contains(query, true) || t.notes.contains(query, true) || t.tagsCsv.contains(query, true)
        val matchesInstrument = instrument == null || t.instrumentType == instrument
        matchesText && matchesInstrument
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, { query = it }, Modifier.weight(1f), placeholder = { Text("Search trades…") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            IconButton(onClick = { showFilters = true }) { Icon(Icons.Default.FilterAlt, "Filters") }
        }
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState("No trades found", if (trades.isEmpty()) "Your saved trades will appear here." else "Try changing your search or filters.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
                items(filtered, key = { it.id }) { trade -> TradeRow(trade, onTradeClick) }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = instrument == null, onClick = { instrument = null }, label = { Text("All") })
                    InstrumentType.values().forEach { type -> FilterChip(selected = instrument == type, onClick = { instrument = type }, label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }) }
                }
                Button(onClick = { showFilters = false }, Modifier.fillMaxWidth()) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun TradeRow(trade: TradeEntity, onClick: (Long) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(trade.id) }) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${trade.symbol} ${trade.optionType?.name.orEmpty().let { if (it.isBlank()) "" else "• $it" }}", style = MaterialTheme.typography.titleMedium)
                Text("${trade.instrumentType.name} • ${trade.direction.name} • qty ${trade.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(money(trade.netPnl), color = if (trade.netPnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        }
    }
}
