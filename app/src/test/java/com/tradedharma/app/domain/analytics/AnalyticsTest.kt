package com.tradedharma.app.domain.analytics

import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.model.TradeDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsTest {
    private fun trade(gross: Double, net: Double, time: Long) = TradeEntity(
        symbol = "NIFTY", exchange = "NSE", instrumentType = InstrumentType.OPTIONS,
        direction = TradeDirection.BUY, quantity = 65.0, entryPrice = 100.0, exitPrice = 100.0,
        charges = gross - net, optionType = null, strikePrice = 24000.0, expiryDate = "2026-09-08", lotSize = 65.0,
        openedAtEpochMs = time, closedAtEpochMs = time, grossPnl = gross, netPnl = net
    )

    @Test fun grossMetricsIgnoreCharges() {
        val trades = listOf(trade(100.0, 90.0, 1), trade(-50.0, -60.0, 2))
        assertEquals(100.0, Analytics.grossProfit(trades), 0.001)
        assertEquals(50.0, Analytics.grossLoss(trades), 0.001)
        assertEquals(2.0, Analytics.profitFactor(trades)!!, 0.001)
        assertEquals(30.0, Analytics.netPnl(trades), 0.001)
    }
}
