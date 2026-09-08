package com.tradedharma.app.domain.calculation

import com.tradedharma.app.domain.model.TradeDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class TradeCalculatorTest {
    @Test fun exactNiftyRegression() {
        val c = TradeCalculator.calculate(TradeDirection.BUY, 65.0, 109.55, 151.40, 76.47, null, null)
        assertEquals(2720.25, c.grossPnl, 0.001)
        assertEquals(2643.78, c.netPnl, 0.001)
    }

    @Test fun buyProfitAndLoss() {
        val profit = TradeCalculator.calculate(TradeDirection.BUY, 10.0, 100.0, 120.0, 5.0, null, null)
        assertEquals(200.0, profit.grossPnl, 0.001)
        assertEquals(195.0, profit.netPnl, 0.001)

        val loss = TradeCalculator.calculate(TradeDirection.BUY, 10.0, 100.0, 80.0, 5.0, null, null)
        assertEquals(-200.0, loss.grossPnl, 0.001)
        assertEquals(-205.0, loss.netPnl, 0.001)
    }

    @Test fun sellProfitAndLoss() {
        val profit = TradeCalculator.calculate(TradeDirection.SELL, 10.0, 100.0, 80.0, 5.0, null, null)
        assertEquals(200.0, profit.grossPnl, 0.001)
        assertEquals(195.0, profit.netPnl, 0.001)

        val loss = TradeCalculator.calculate(TradeDirection.SELL, 10.0, 80.0, 100.0, 5.0, null, null)
        assertEquals(-200.0, loss.grossPnl, 0.001)
        assertEquals(-205.0, loss.netPnl, 0.001)
    }

    @Test fun multipleLotsAndRr() {
        val c = TradeCalculator.calculate(TradeDirection.BUY, 65.0 * 3.0, 109.55, 151.40, 76.47, 100.0, 180.0)
        assertEquals(8160.75, c.grossPnl, 0.001)
        assertEquals(8084.28, c.netPnl, 0.001)
        assertEquals(1862.25, c.initialRisk!!, 0.001)
    }

    @Test fun zeroPnlIsHandled() {
        val c = TradeCalculator.calculate(TradeDirection.BUY, 1.0, 100.0, 100.0, 0.0, null, null)
        assertEquals(0.0, c.grossPnl, 0.001)
        assertEquals(0.0, c.netPnl, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroQuantity() {
        TradeCalculator.calculate(TradeDirection.BUY, 0.0, 10.0, 12.0, 0.0, null, null)
    }
}
