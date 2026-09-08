package com.tradedharma.app.domain.analytics

import com.tradedharma.app.data.local.TradeEntity
import kotlin.math.max

object Analytics {
    fun wins(trades: List<TradeEntity>) = trades.count { it.netPnl > 0 }
    fun losses(trades: List<TradeEntity>) = trades.count { it.netPnl < 0 }
    fun breakeven(trades: List<TradeEntity>) = trades.count { it.netPnl == 0.0 }
    fun winRate(trades: List<TradeEntity>): Double = if (trades.isEmpty()) 0.0 else wins(trades) * 100.0 / trades.size
    fun netPnl(trades: List<TradeEntity>) = trades.sumOf { it.netPnl }
    fun grossProfit(trades: List<TradeEntity>) = trades.filter { it.grossPnl > 0 }.sumOf { it.grossPnl }
    fun grossLoss(trades: List<TradeEntity>) = -trades.filter { it.grossPnl < 0 }.sumOf { it.grossPnl }
    fun profitFactor(trades: List<TradeEntity>): Double? {
        val loss = grossLoss(trades)
        return if (loss > 0) grossProfit(trades) / loss else null
    }
    fun averageWin(trades: List<TradeEntity>): Double? = trades.filter { it.netPnl > 0 }.map { it.netPnl }.averageOrNull()
    fun averageLoss(trades: List<TradeEntity>): Double? = trades.filter { it.netPnl < 0 }.map { it.netPnl }.averageOrNull()
    fun expectancy(trades: List<TradeEntity>): Double? {
        if (trades.isEmpty()) return null
        val pWin = wins(trades).toDouble() / trades.size
        val pLoss = losses(trades).toDouble() / trades.size
        val aw = averageWin(trades) ?: 0.0
        val al = averageLoss(trades)?.let { -it } ?: 0.0
        return pWin * aw - pLoss * al
    }

    fun maxDrawdown(trades: List<TradeEntity>): Double {
        var equity = 0.0
        var peak = 0.0
        var maxDd = 0.0
        trades.sortedBy { it.closedAtEpochMs ?: it.openedAtEpochMs }.forEach {
            equity += it.netPnl
            peak = max(peak, equity)
            maxDd = max(maxDd, peak - equity)
        }
        return maxDd
    }

    fun equityPoints(trades: List<TradeEntity>): List<Double> {
        var running = 0.0
        return trades.sortedBy { it.closedAtEpochMs ?: it.openedAtEpochMs }.map {
            running += it.netPnl
            running
        }
    }

    fun byStrategy(trades: List<TradeEntity>) = trades.groupBy { it.strategy?.ifBlank { "Unspecified" } ?: "Unspecified" }
        .mapValues { (_, list) -> Summary(list) }.toList().sortedByDescending { it.second.netPnl }

    fun byTag(trades: List<TradeEntity>) = trades.flatMap { t -> t.tags().map { it to t } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, list) -> Summary(list) }.toList().sortedByDescending { it.second.netPnl }

    data class Summary(val trades: List<TradeEntity>) {
        val count get() = trades.size
        val winRate get() = if (count == 0) 0.0 else wins(trades) * 100.0 / count
        val netPnl get() = netPnl(trades)
    }

    private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()
}
