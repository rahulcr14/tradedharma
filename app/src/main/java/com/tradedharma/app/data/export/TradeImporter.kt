package com.tradedharma.app.data.export

import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.model.OptionType
import com.tradedharma.app.domain.model.TradeDirection
import org.json.JSONArray
import org.json.JSONObject

object TradeImporter {
    fun json(input: String): List<TradeEntity> {
        val root = JSONObject(input)
        val arr = root.optJSONArray("trades") ?: JSONArray()
        val output = mutableListOf<TradeEntity>()
        for (i in 0 until arr.length()) output += fromJson(arr.getJSONObject(i))
        return output
    }

    fun csv(input: String): List<TradeEntity> {
        val rows = parseCsv(input.trim())
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.trim() }
        val index = header.withIndex().associate { it.value to it.index }
        fun value(row: List<String>, key: String): String = index[key]?.let { row.getOrNull(it).orEmpty() }.orEmpty()
        return rows.drop(1).filter { it.any(String::isNotBlank) }.map { r ->
            TradeEntity(
                id = 0,
                symbol = value(r, "symbol").ifBlank { error("CSV missing symbol") },
                exchange = value(r, "exchange").ifBlank { "NSE" },
                instrumentType = InstrumentType.valueOf(value(r, "instrumentType").ifBlank { "EQUITY" }),
                direction = TradeDirection.valueOf(value(r, "direction").ifBlank { "BUY" }),
                quantity = value(r, "quantity").toDouble(), entryPrice = value(r, "entryPrice").toDouble(), exitPrice = value(r, "exitPrice").toDouble(),
                stopLoss = value(r, "stopLoss").toDoubleOrNull(), target = value(r, "target").toDoubleOrNull(), charges = value(r, "charges").toDoubleOrNull() ?: 0.0,
                strategy = value(r, "strategy").ifBlank { null }, tagsCsv = value(r, "tags"), notes = value(r, "notes"),
                optionType = value(r, "optionType").takeIf { it.isNotBlank() }?.let { OptionType.valueOf(it) }, strikePrice = value(r, "strikePrice").toDoubleOrNull(), expiryDate = value(r, "expiryDate").ifBlank { null }, lotSize = value(r, "lotSize").toDoubleOrNull(),
                openedAtEpochMs = value(r, "openedAtEpochMs").toLongOrNull() ?: System.currentTimeMillis(), closedAtEpochMs = value(r, "closedAtEpochMs").toLongOrNull(),
                grossPnl = value(r, "grossPnl").toDouble(), netPnl = value(r, "netPnl").toDouble(), initialRisk = value(r, "initialRisk").toDoubleOrNull(), plannedReward = value(r, "plannedReward").toDoubleOrNull(), plannedRr = value(r, "plannedRr").toDoubleOrNull(), actualRMultiple = value(r, "actualRMultiple").toDoubleOrNull(), screenshotPath = value(r, "screenshotPath").ifBlank { null }
            )
        }
    }

    private fun fromJson(o: JSONObject) = TradeEntity(
        id = 0, symbol = o.optString("symbol"), exchange = o.optString("exchange", "NSE"),
        instrumentType = InstrumentType.valueOf(o.optString("instrumentType", InstrumentType.EQUITY.name)),
        direction = TradeDirection.valueOf(o.optString("direction", TradeDirection.BUY.name)),
        quantity = o.optDouble("quantity", 0.0), entryPrice = o.optDouble("entryPrice", 0.0), exitPrice = o.optDouble("exitPrice", 0.0),
        stopLoss = optNullableDouble(o, "stopLoss"), target = optNullableDouble(o, "target"), charges = o.optDouble("charges", 0.0),
        strategy = o.optString("strategy").ifBlank { null }, tagsCsv = o.optString("tagsCsv"), notes = o.optString("notes"),
        optionType = o.optString("optionType").takeIf { it.isNotBlank() && it != "null" }?.let { OptionType.valueOf(it) },
        strikePrice = optNullableDouble(o, "strikePrice"), expiryDate = o.optString("expiryDate").ifBlank { null }, lotSize = optNullableDouble(o, "lotSize"),
        openedAtEpochMs = o.optLong("openedAtEpochMs", System.currentTimeMillis()), closedAtEpochMs = o.optLong("closedAtEpochMs", 0).takeIf { it > 0 },
        grossPnl = o.optDouble("grossPnl", 0.0), netPnl = o.optDouble("netPnl", 0.0), initialRisk = optNullableDouble(o, "initialRisk"), plannedReward = optNullableDouble(o, "plannedReward"), plannedRr = optNullableDouble(o, "plannedRr"), actualRMultiple = optNullableDouble(o, "actualRMultiple"), screenshotPath = o.optString("screenshotPath").ifBlank { null }
    )

    private fun optNullableDouble(o: JSONObject, key: String): Double? = if (o.isNull(key)) null else o.optDouble(key).let { if (it.isNaN()) null else it }

    private fun parseCsv(input: String): List<List<String>> {
        val rows = mutableListOf<List<String>>(); var field = StringBuilder(); var row = mutableListOf<String>(); var quoted = false; var i = 0
        fun flushField() { row += field.toString(); field = StringBuilder() }
        while (i < input.length) {
            val c = input[i]
            when {
                c == '"' && quoted && i + 1 < input.length && input[i + 1] == '"' -> { field.append('"'); i++ }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> flushField()
                c == '\n' && !quoted -> { flushField(); rows += row; row = mutableListOf() }
                c != '\r' -> field.append(c)
            }
            i++
        }
        flushField(); if (row.isNotEmpty()) rows += row
        return rows
    }
}
