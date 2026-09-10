package com.tradedharma.app.data.export

import com.tradedharma.app.data.local.TradeEntity
import org.json.JSONArray
import org.json.JSONObject

object TradeExporter {
    fun csv(trades: List<TradeEntity>): String {
        val header = listOf("id","symbol","exchange","instrumentType","direction","quantity","entryPrice","exitPrice","stopLoss","target","charges","strategy","tags","notes","optionType","strikePrice","expiryDate","lotSize","tradeDate","openedAtEpochMs","closedAtEpochMs","grossPnl","netPnl","initialRisk","plannedReward","plannedRr","actualRMultiple","screenshotPath").joinToString(",")
        val rows = trades.map { t ->
            listOf(t.id,t.symbol,t.exchange,t.instrumentType.name,t.direction.name,t.quantity,t.entryPrice,t.exitPrice,t.stopLoss,t.target,t.charges,t.strategy,t.tagsCsv,t.notes,t.optionType?.name,t.strikePrice,t.expiryDate,t.lotSize,t.tradeDate,t.openedAtEpochMs,t.closedAtEpochMs,t.grossPnl,t.netPnl,t.initialRisk,t.plannedReward,t.plannedRr,t.actualRMultiple,t.screenshotPath)
                .joinToString(",") { escape(it?.toString() ?: "") }
        }
        return header + "\n" + rows.joinToString("\n")
    }

    fun json(trades: List<TradeEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        val array = JSONArray()
        trades.forEach { t ->
            array.put(JSONObject().apply {
                put("id", t.id); put("symbol", t.symbol); put("exchange", t.exchange)
                put("instrumentType", t.instrumentType.name); put("direction", t.direction.name)
                put("quantity", t.quantity); put("entryPrice", t.entryPrice); put("exitPrice", t.exitPrice)
                put("stopLoss", t.stopLoss); put("target", t.target); put("charges", t.charges)
                put("strategy", t.strategy); put("tagsCsv", t.tagsCsv); put("notes", t.notes)
                put("optionType", t.optionType?.name); put("strikePrice", t.strikePrice); put("expiryDate", t.expiryDate); put("lotSize", t.lotSize)
                put("tradeDate", t.tradeDate)
                put("openedAtEpochMs", t.openedAtEpochMs); put("closedAtEpochMs", t.closedAtEpochMs)
                put("grossPnl", t.grossPnl); put("netPnl", t.netPnl); put("initialRisk", t.initialRisk); put("plannedReward", t.plannedReward); put("plannedRr", t.plannedRr); put("actualRMultiple", t.actualRMultiple); put("screenshotPath", t.screenshotPath)
            })
        }
        root.put("trades", array)
        return root.toString(2)
    }

    private fun escape(value: String): String = if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"${value.replace("\"", "\"\"")}\"" else value
}
