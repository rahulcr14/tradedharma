package com.tradedharma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.model.OptionType
import com.tradedharma.app.domain.model.TradeDirection

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val exchange: String = "NSE",
    val instrumentType: InstrumentType,
    val direction: TradeDirection,
    val quantity: Double,
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLoss: Double? = null,
    val target: Double? = null,
    val charges: Double = 0.0,
    val strategy: String? = null,
    val tagsCsv: String = "",
    val notes: String = "",
    val optionType: OptionType? = null,
    val strikePrice: Double? = null,
    val expiryDate: String? = null,
    val lotSize: Double? = null,
    val screenshotPath: String? = null,
    val openedAtEpochMs: Long,
    val closedAtEpochMs: Long? = null,
    val grossPnl: Double,
    val netPnl: Double,
    val initialRisk: Double? = null,
    val plannedReward: Double? = null,
    val plannedRr: Double? = null,
    val actualRMultiple: Double? = null,
    val tradeDate: String? = null
) {
    fun tags(): List<String> = tagsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
