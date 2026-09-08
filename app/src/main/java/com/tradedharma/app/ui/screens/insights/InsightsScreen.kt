package com.tradedharma.app.ui.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.analytics.Analytics
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.components.EmptyState
import com.tradedharma.app.ui.components.MetricCard
import com.tradedharma.app.ui.components.money
import java.util.Calendar

@Composable
fun InsightsScreen(repository: TradeRepository) {
    val trades = repository.observeTrades().collectAsStateWithLifecycle(emptyList()).value
    if (trades.isEmpty()) {
        EmptyState("Insights need data", "Record a few trades and the app will surface patterns.")
        return
    }
    val strategies = Analytics.byStrategy(trades)
    val tags = Analytics.byTag(trades)
    val instruments = trades.groupBy { it.instrumentType }.mapValues { Analytics.Summary(it.value) }.toList().sortedByDescending { it.second.netPnl }
    val times = trades.groupBy { hourBucket(it) }.mapValues { Analytics.Summary(it.value) }.toList().sortedBy { it.first }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Expectancy", Analytics.expectancy(trades)?.let { money(it) } ?: "—", Modifier.weight(1f))
                MetricCard("Max Drawdown", money(-Analytics.maxDrawdown(trades)), Modifier.weight(1f))
            }
        }
        item { Text("By strategy", style = MaterialTheme.typography.titleLarge) }
        items(strategies) { (name, summary) -> InsightRow(name, summary.count, summary.winRate, summary.netPnl) }
        item { Text("By instrument", style = MaterialTheme.typography.titleLarge) }
        items(instruments) { (name, summary) -> InsightRow(name.name.lowercase().replaceFirstChar { it.uppercase() }, summary.count, summary.winRate, summary.netPnl) }
        item { Text("By time of day", style = MaterialTheme.typography.titleLarge) }
        items(times) { (name, summary) -> InsightRow(name, summary.count, summary.winRate, summary.netPnl) }
        item { Text("Behavior & tags", style = MaterialTheme.typography.titleLarge) }
        if (tags.isEmpty()) item { Text("Add tags such as FOMO, Followed Plan, Trending or Moved SL to unlock behavioral insights.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(tags.take(12)) { (name, summary) -> InsightRow(name, summary.count, summary.winRate, summary.netPnl) }
    }
}

private fun hourBucket(trade: TradeEntity): String {
    val c = Calendar.getInstance().apply { timeInMillis = trade.openedAtEpochMs }
    val h = c.get(Calendar.HOUR_OF_DAY)
    val start = (h / 1).coerceIn(0, 23)
    return "%02d:00–%02d:00".format(start, (start + 1) % 24)
}

@Composable
private fun InsightRow(name: String, count: Int, winRate: Double, net: Double) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(name, style = MaterialTheme.typography.titleMedium); Text("$count trades • ${"%.1f".format(winRate)}% win rate", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(money(net), color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}
