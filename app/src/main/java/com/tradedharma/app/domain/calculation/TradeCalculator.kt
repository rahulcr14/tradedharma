package com.tradedharma.app.domain.calculation

import com.tradedharma.app.domain.model.TradeDirection
import kotlin.math.abs

data class TradeCalculation(
    val grossPnl: Double,
    val netPnl: Double,
    val initialRisk: Double?,
    val plannedReward: Double?,
    val plannedRr: Double?,
    val actualRMultiple: Double?
)

object TradeCalculator {
    fun calculate(
        direction: TradeDirection,
        quantity: Double,
        entry: Double,
        exit: Double,
        charges: Double,
        stopLoss: Double?,
        target: Double?
    ): TradeCalculation {
        require(quantity > 0) { "Quantity must be greater than zero" }
        require(entry >= 0 && exit >= 0) { "Prices cannot be negative" }
        require(charges >= 0) { "Charges cannot be negative" }
        stopLoss?.let { require(it >= 0) { "Stop loss cannot be negative" } }
        target?.let { require(it >= 0) { "Target cannot be negative" } }

        val gross = when (direction) {
            TradeDirection.BUY -> (exit - entry) * quantity
            TradeDirection.SELL -> (entry - exit) * quantity
        }
        val net = gross - charges
        val risk = stopLoss?.let { abs(entry - it) * quantity }?.takeIf { it > 0 }
        val reward = target?.let { abs(it - entry) * quantity }
        val rr = if (risk != null && reward != null) reward / risk else null
        val rMultiple = if (risk != null) net / risk else null
        return TradeCalculation(gross, net, risk, reward, rr, rMultiple)
    }
}
