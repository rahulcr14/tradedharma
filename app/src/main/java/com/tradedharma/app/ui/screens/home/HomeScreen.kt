package com.tradedharma.app.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tradedharma.app.domain.analytics.Analytics
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.components.EmptyState
import com.tradedharma.app.ui.components.MetricCard
import com.tradedharma.app.ui.components.money

@Composable
fun HomeScreen(repository: TradeRepository, onAddTrade: () -> Unit) {
    val trades = repository.observeTrades().collectAsStateWithLifecycle(emptyList()).value
    val net = Analytics.netPnl(trades)
    val pf = Analytics.profitFactor(trades)
    val points = Analytics.equityPoints(trades)

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
        item { Text("Good morning ☀️", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground) }
        item { Text("Discipline today, freedom tomorrow.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (trades.isEmpty()) {
            item { EmptyState("Start your journal", "Record your first trade to unlock P&L, equity and insights.", "Add first trade", onAddTrade) }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Total Trades", trades.size.toString(), Modifier.weight(1f))
                    MetricCard("Win Rate", "%.1f%%".format(Analytics.winRate(trades)), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Net P&L", money(net), Modifier.weight(1f))
                    MetricCard("Profit Factor", pf?.let { "%.2f".format(it) } ?: "—", Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Max Drawdown", money(-Analytics.maxDrawdown(trades)), Modifier.weight(1f))
                    MetricCard("Expectancy", Analytics.expectancy(trades)?.let { money(it) } ?: "—", Modifier.weight(1f))
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Equity Curve", style = MaterialTheme.typography.titleMedium)
                            Text(money(net), color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        EquityCurve(points)
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Win / Loss Distribution", style = MaterialTheme.typography.titleMedium)
                        val total = trades.size.coerceAtLeast(1)
                        val winFraction = Analytics.wins(trades).toFloat() / total
                        LinearProgressIndicator({ winFraction }, Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${Analytics.wins(trades)} Wins")
                            Text("${Analytics.losses(trades)} Losses")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquityCurve(points: List<Double>) {
    if (points.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(180.dp)) {
        val min = points.minOrNull() ?: 0.0
        val max = points.maxOrNull() ?: 0.0
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = if (points.size == 1) 0f else size.width * index / (points.size - 1).toFloat()
            val y = size.height - ((value - min) / range).toFloat() * (size.height - 12.dp.toPx()) - 6.dp.toPx()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        drawLine(outlineColor, Offset(0f, size.height - 1), Offset(size.width, size.height - 1))
    }
}
